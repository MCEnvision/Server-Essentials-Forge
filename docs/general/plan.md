# Server Essentials Forge Network Plan

> **Plan ID:** PLAN-MASTER  
> **Plan status:** VALIDATED  
> **Project state:** EXISTING  
> **Planning subject:** SEF Forge 1.20.1 server-only backport, Velocity companion, comprehensive audit and hierarchical RTP  
> **Plan profile:** software_product  
> **Diagnostics contract:** 2

## 1. Project Identity

```text
Project: Server Essentials Forge, mod ID sef
Requested artifact: authoritative_plan
Repository root: /mnt/hermes/projects/Sef/forge-1.20.1/1.1
Starting branch: envy/sef-network-plan
Starting commit: 1e8bab26d9d6ff6b1bf1d5ef41eb8d6c1a51ad98
Authoritative remote:
origin
https://github.com/MCEnvision/Server-Essentials-Forge.git
Remote ref: origin/forge-1.20.1
Remote commit: 1e8bab26d9d6ff6b1bf1d5ef41eb8d6c1a51ad98
Backend product branch: forge-1.20.1
Proxy product branch: velocity-latest
Authoritative plan: docs/general/plan.md
Execution blueprints: docs/general/phases/plan-phase-NNN.md
Manifest: docs/general/plan.index.json
Deterministic handoff: docs/general/plan.handoff.json
Read-only implementation reference: /mnt/hermes/projects/SEFPORTED
Reference commit: e160a235b19c992b3a23c3a43754e92ad0147948
```

The existing Forge product is the destination. Its baseline is Minecraft 1.20.1, Forge 47.3.12, Java 17 and mod version 1.1. The reference is a newer implementation whose executed features must be inventoried before backporting. Its branch, uncommitted work, plan and saved goal remain untouched. The owner-approved `/mnt/hermes/projects/SefVelocity` directory may hold a linked worktree of this repository for the applicable `envy` Velocity phase branch. This exact placement exception does not create a separate repository. Phase 004 must recheck contents, active ownership and lineage before allocation, preserve the owner directory and user files, and exclude owned disposable build or runtime children from source scans and packaging. The verified relocation maps the former `/mnt/hermes/projects/Sef` checkout to `/mnt/hermes/projects/Sef/forge-1.20.1/1.1`; the latter is the canonical plan and repository anchor, while the former is only its project container. This plan does not rename the repository default branch, create a saved goal or activate execution.

## 2. Planning Subject and Source Roles

| ID | Role | Subject | Source | Intended use |
|---|---|---|---|---|
| SRC-001 | owner_request | Backport, removals, network commands, shared state, cross-server homes, and plugin branch | Approved product decisions recorded in research/sources/owner-request.md | Mandatory scope, exclusions, topology, and delegated storage selection |
| SRC-002 | repository_evidence | Existing Forge 1.20.1 destination | /mnt/hermes/projects/Sef at 1e8bab26d9d6ff6b1bf1d5ef41eb8d6c1a51ad98 | Baseline platform, retained behavior, build and branch identity |
| SRC-003 | repository_evidence | SEFPORTED source implementation | /mnt/hermes/projects/SEFPORTED at e160a235b19c992b3a23c3a43754e92ad0147948 | Read-only backport source, implemented feature inventory and removal dependencies, never a competing active plan |
| SRC-004 | repository_evidence | Authorized development hosts and repository capabilities | research/sources/environment-observations.md | Planning prerequisite observations and exact later verification boundaries |
| SRC-101 | reference | Native Forge handshake compatibility excludes target 1.20.1. Ambassador is documented alternative. | https://docs.papermc.io/velocity/server-compatibility/ section Minecraft Forge, lines 128 to 133 in fetched document. | Support the referenced backport, compatibility, removal or network design finding. Source text is evidence, not authority. |
| SRC-102 | reference | Modern forwarding authenticates forwarded identity; backend needs compatible implementation. Paper configuration does not apply to Forge. | https://docs.papermc.io/velocity/player-information-forwarding/ lines 74 to 90 and 109 to 113. | Support the referenced backport, compatibility, removal or network design finding. Source text is evidence, not authority. |
| SRC-103 | reference | Ambassador and PCF have distinct roles. PCF includes command argument wrapping. Mod argument ID conflicts remain a compatibility risk. | https://raw.githubusercontent.com/adde0109/Proxy-Compatible-Forge/main/README.md lines 29 to 57, 64 to 94. | Support the referenced backport, compatibility, removal or network design finding. Source text is evidence, not authority. |
| SRC-104 | reference | Current runtime requires Java 25. | https://docs.papermc.io/velocity/getting-started/ line 77. | Support the referenced backport, compatibility, removal or network design finding. Source text is evidence, not authority. |
| SRC-105 | reference | Advertised latest is 4.2.1-SNAPSHOT build 32, not evidence of stable selection. | https://papermc.io/downloads/velocity | Support the referenced backport, compatibility, removal or network design finding. Source text is evidence, not authority. |
| SRC-106 | reference | API returned build 30, channel STABLE, published 2026-09-14T16:50:54.749Z. Artifact velocity-4.2.0-30.jar, SHA-256 35a5596a5468a035d8a32c8de5ebb0dc6b8d8f0cc3ff5169d514aca762af8aa8. | https://fill.papermc.io/v3/projects/velocity and https://fill.papermc.io/v3/projects/velocity/versions/4.2.0/builds | Support the referenced backport, compatibility, removal or network design finding. Source text is evidence, not authority. |
| SRC-107 | reference | Latest release observed 1.4.5, version ID YeQbhgna, artifact Ambassador-Velocity-1.4.5-all.jar. SHA-512 214081b498644c5c640e1aea23deff0469d1c842e1142aae742143c8738a971a4898ead72da1605f1c2c202c4f1be8bcf28acc2787cbcede2a86d642ae48f81d. Free direct download available. | https://api.modrinth.com/v2/project/ambassador/version and https://github.com/adde0109/Ambassador/releases/tag/v1.4.5 | Support the referenced backport, compatibility, removal or network design finding. Source text is evidence, not authority. |
| SRC-108 | reference | Project declares LGPL-2.1-or-later and documents matching-pack switching without an added client mod in some configurations. This is not proof that the pinned runtime can complete an in-session switch; the approved client reset adapter is now in scope. Do not use stale main README, which describes an obsolete alpha architecture. | https://modrinth.com/plugin/ambassador and https://github.com/adde0109/Ambassador/blob/non-api/README.md | Support the referenced backport, compatibility, removal or network design finding. Source text is evidence, not authority. |
| SRC-010 | owner_request | Approves Ambassador, ProxyCompatibleForge and the linked Forge 1.20.1 client reset file for the network compatibility matrix, then clarifies they are optional deployment dependencies for standalone Forge. Configured Ambassador mode must ask operators to install the matching SEF Velocity companion when it cannot be verified. This permits an external client compatibility mod in the selected proxy profile, not SEF client code or arbitrary pack replacement. | Approved compatibility clarification in research/sources/client-reset-amendment.md, observed 2026-09-25 | Resolve DEC-007 and the early transfer fixture while requiring standalone Forge without proxy adapters and operator-only companion installation guidance for configured network mode. |
| SRC-118 | reference | CurseForge file 4657349 is ForgeClientResetPacket-0.3.0.jar for Forge 1.20 and 1.20.1, client-only, with project MIT metadata. Its archive declares mod ID clientresetpacket and internal version 0.0.6. A later 0.3.1 file exists but is not this owner-linked candidate. | https://www.curseforge.com/minecraft/mc-mods/forge-client-reset-packet-forward/files/4657349 and research/sources/client-reset-amendment.md | Pin exact candidate identity; metadata and a valid archive do not prove compatibility with the selected pack. |
| SRC-119 | reference | Ambassador 1.4.5 selects reset-capable switching only for the advertised clientresetpacket mod ID; without reset or redirect capability its pinned source may disconnect with a reconnect instruction. | https://github.com/adde0109/Ambassador/blob/v1.4.5/src/main/java/org/adde0109/ambassador/forge/VelocityForgeClientConnectionPhase.java and research/sources/client-reset-amendment.md | Require real round-trip proof and early SEF admission refusal when reset capability is absent. |
| SRC-109 | reference | Latest matching release observed 1.3.1, ID qiZ49HIW, published 2026-08-31. Artifact proxy-compatible-forge-1.3.1.jar, SHA-512 dba5b70fedcb0f5fdc014801ab43e6d048f24b531de8aea5b70ea223760b66ae9fba3033c2ccd9d78793b9ea2f9d3b47ae1f4caa95aa129d989f97e63636b4ab. Listed dependencies are optional. | https://api.modrinth.com/v2/project/proxy-compatible-forge/version?loaders=%5B%22forge%22%5D&game_versions=%5B%221.20.1%22%5D | Support the referenced backport, compatibility, removal or network design finding. Source text is evidence, not authority. |
| SRC-110 | reference | PCF license preamble defaults to LGPL 2.1 or later with original licenses retained for imported code. Ambassador development depends on velocity-proxy internals. Keep dependencies separately installed; do not copy or shade their implementation without a separate license inventory. | https://api.github.com/repos/adde0109/Proxy-Compatible-Forge/license and https://github.com/adde0109/Ambassador/blob/non-api/build.gradle.kts | Support the referenced backport, compatibility, removal or network design finding. Source text is evidence, not authority. |
| SRC-111 | reference | Consume administrative channel before source checks, reject player-origin messages, validate server connection. Backend sends use connected player carriers. | https://docs.papermc.io/velocity/dev/plugin-messaging/ lines 77 to 84, 163 to 218, 249 to 297. | Support the referenced backport, compatibility, removal or network design finding. Source text is evidence, not authority. |
| SRC-112 | reference | Server logical side, main Forge bus, cancellable. This branch source still needs exact Forge 47.3.12 source confirmation during implementation. | https://raw.githubusercontent.com/MinecraftForge/MinecraftForge/1.20.x/src/main/java/net/minecraftforge/event/ServerChatEvent.java lines 15 to 25. | Support the referenced backport, compatibility, removal or network design finding. Source text is evidence, not authority. |
| SRC-113 | reference | Signed-chat denial warning. Current deployment API must still be pinned and compiled against, not inferred from historical javadocs. | https://jd.papermc.io/velocity/3.4.0/com/velocitypowered/api/event/player/PlayerChatEvent.html lines 121 to 127. | Support the referenced backport, compatibility, removal or network design finding. Source text is evidence, not authority. |
| SRC-114 | reference | LoginEvent follows authentication and precedes backend connect. PostConnect is not awaited. connect() returns a result after login and requires explicit error handling. Disconnect generally runs asynchronously and player mutations are undefined. | https://raw.githubusercontent.com/PaperMC/Velocity/dev/4.0.0/api/src/main/java/com/velocitypowered/api/event/connection/LoginEvent.java, https://raw.githubusercontent.com/PaperMC/Velocity/dev/4.0.0/api/src/main/java/com/velocitypowered/api/event/player/ServerPostConnectEvent.java, https://raw.githubusercontent.com/PaperMC/Velocity/dev/4.0.0/api/src/main/java/com/velocitypowered/api/proxy/ConnectionRequestBuilder.java, https://jd.papermc.io/velocity/3.4.0/com/velocitypowered/api/event/connection/DisconnectEvent.html | Support the referenced backport, compatibility, removal or network design finding. Source text is evidence, not authority. |
| SRC-115 | reference | Local embedded data versus client/server database tradeoff. | https://www.sqlite.org/whentouse.html | Support the referenced backport, compatibility, removal or network design finding. Source text is evidence, not authority. |
| SRC-116 | reference | WAL operational and host-local requirements. Use supported backup procedures and do not copy only a live main database file. | https://www.sqlite.org/wal.html | Support the referenced backport, compatibility, removal or network design finding. Source text is evidence, not authority. |
| SRC-201 | repository_evidence | Economy construction, cost adapters, storage registration, command cost schedule and GUI descriptors are kernel dependencies. Removing only economy commands leaves live services and configuration behind. Preserve ordinary permission, cooldown and confirmation policies while removing monetary policy entirely. | src/main/java/com/enviouse/sef/kernel/KernelServices.java:117, :179, :205, :338, :347, :470, :1100 | Support the referenced backport, compatibility, removal or network design finding. Source text is evidence, not authority. |
| SRC-202 | repository_evidence | GUI/economy/HUD/Fancy Tags module declarations exist; many retained modules declare GUI dependencies and generic GUI keys. Removal must rebuild dependencies and generated config, not merely disable modules. Preserve legacy files without reading their monetary values into active state or deleting owner data. | src/main/java/com/enviouse/sef/config/modules/ModuleConfigRegistry.java:21, :213, :603 and ModuleConfigService.java:65 | Support the referenced backport, compatibility, removal or network design finding. Source text is evidence, not authority. |
| SRC-203 | repository_evidence | Home ownership, revision and tombstone exist, but location has dimension plus coordinates and rotation only. There is no server identity. Safe teleport validates local dimensions, loaded chunks, claims, hazards and bounds. A remote home cannot be resolved by sending the existing local record to another server. | src/main/java/com/enviouse/sef/teleport/HomeRecord.java:9, SavedLocation.java:10, SafeTeleportService.java:49 | Support the referenced backport, compatibility, removal or network design finding. Source text is evidence, not authority. |
| SRC-204 | repository_evidence | Existing backend chat flow already checks mute and cancels events. Network moderation should feed one authoritative enforcement decision rather than stack contradictory local and network mute managers. Command aliases and third-party chat paths still require coverage. | src/main/java/com/enviouse/sef/events/ChatEventHandler.java:70, :111, :136 | Support the referenced backport, compatibility, removal or network design finding. Source text is evidence, not authority. |
| SRC-205 | repository_evidence | Visibility depends on observer permissions; current persistence is player NBT and runtime maps. Network revisioned vanish state must become the authority in network mode, with reconnect application before public visibility. | src/main/java/com/enviouse/sef/vanish/VanishUtil.java:138, :194, :209, :229 | Support the referenced backport, compatibility, removal or network design finding. Source text is evidence, not authority. |
| SRC-206 | repository_evidence | Existing hooks filter vanilla online name suggestions and selector targeting. Every copied mixin needs Forge 1.20.1 descriptor validation. Proxy suggestions and SEF own target lists need the same visibility predicate. | src/main/java/com/enviouse/sef/vanish/mixin/chat/CommandSourceStackMixin.java:33, EntitySelectorMixin.java:20 | Support the referenced backport, compatibility, removal or network design finding. Source text is evidence, not authority. |
| SRC-207 | repository_evidence | NeoForge payload registration, custom configuration tasks, 1.21 data components and registry-aware snapshot serialization cannot be copied unchanged. Remove client protocol entirely; independently adapt retained item and recovery paths to 1.20.1 NBT and Forge events. | src/main/java/com/enviouse/sef/ServerEssentialsForge.java:73, gui/protocol/SefNetwork.java:7, inventory/InventoryUtilityCommands.java:19, recovery/ItemStackSnapshotCodec.java:26 | Support the referenced backport, compatibility, removal or network design finding. Source text is evidence, not authority. |
| SRC-208 | repository_evidence | Existing target gives Forge architecture and existing vanish command examples, not parity with the richer source port. Source inventory must classify every feature as retained, removed by owner, unavailable, or requiring port work. | Target src/main/java/com/enviouse/sef/vanish/VanishCommand.java:38, events/ChatEventHandler.java:34, gradle.properties | Support the referenced backport, compatibility, removal or network design finding. Source text is evidence, not authority. |
| SRC-005 | owner_request | Comprehensive CoreProtect-style audit module and approved modpack compatibility boundary | Approved audit scope, 2026-09-24, recorded in research/sources/audit-owner-request.md | Initial audit scope and compatible-modpack boundary. Expanded owner decisions resolve its initial open questions. |
| SRC-301 | reference | Investigation supports inspection, lookup, actor/time/radius/action/material filters, pagination, counts and summaries. Rollback, restore, undo and purge are distinct mutating operations, not implied by lookup. | https://docs.coreprotect.net/commands/ | Support the comprehensive Forge-native audit capture, investigation, durability, security and regression contract. Reference behavior does not authorize destructive features or copying implementation. |
| SRC-302 | reference | Logging families include blocks, natural changes, pistons, dispensers, fire, explosions, entity changes/kills/spawns, signs, buckets, growth, portals, fluid flow, item/hopper transactions, drops/pickups, interactions, chat, commands, sessions and username changes. The existence of a switch does not prove every mod integration. | https://github.com/PlayPro/CoreProtect/blob/68739a2596929f6b995986b0dc5b09d343956103/src/main/java/net/coreprotect/config/Config.java lines 75 to 110 | Support the comprehensive Forge-native audit capture, investigation, durability, security and regression contract. Reference behavior does not authorize destructive features or copying implementation. |
| SRC-303 | reference | Current docs describe SQLite/MySQL and version 25 DuckDB/ClickHouse options, with version-specific migration and purge restrictions. They must not be treated as the behavior of a 1.20.1-compatible release. These are operational references, not dependencies mandated for SEF. | https://docs.coreprotect.net/config/ and https://docs.coreprotect.net/auto-purge/ | Support the comprehensive Forge-native audit capture, investigation, durability, security and regression contract. Reference behavior does not authorize destructive features or copying implementation. |
| SRC-304 | reference | Versioned API exposes Bukkit plugin access and world/block types plus logging/lookups/rollback/restore. It is not a Forge API. Queue lookup distinguishes unsaved from database history. An extension API is necessary for custom changes. | https://docs.coreprotect.net/api/version/v10/ | Support the comprehensive Forge-native audit capture, investigation, durability, security and regression contract. Reference behavior does not authorize destructive features or copying implementation. |
| SRC-305 | reference | OBSERVED correction to assignment premise: current upstream license is Artistic License 2.0, not GPL. No license compatibility conclusion is asserted. Use original Forge-native implementation; do not copy source or present the module as the official CoreProtect product. | https://github.com/PlayPro/CoreProtect/blob/68739a2596929f6b995986b0dc5b09d343956103/LICENSE | Support the comprehensive Forge-native audit capture, investigation, durability, security and regression contract. Reference behavior does not authorize destructive features or copying implementation. |
| SRC-306 | reference | Release notes report corrected hopper counts, missing hopper records during breaks/shutdown, same-coordinate different-world errors, and bucket false positives. These are concrete regression scenarios, not proof SEF has these bugs. | https://github.com/PlayPro/CoreProtect/releases/tag/v24.1 | Support the comprehensive Forge-native audit capture, investigation, durability, security and regression contract. Reference behavior does not authorize destructive features or copying implementation. |
| SRC-307 | reference | Inspection, lookup categories, rollback, restore and purge have distinct permission surfaces. SEF should separately gate metadata, sensitive content, vanished activity, export and retention administration. | https://docs.coreprotect.net/permissions/ | Support the comprehensive Forge-native audit capture, investigation, durability, security and regression contract. Reference behavior does not authorize destructive features or copying implementation. |
| SRC-308 | reference | Break is before mutation and cancellable; placement can be cancelled; multi-placement exposes multiple snapshots; crop growth has a post event; tool modification can be simulated. Intent events alone cannot certify committed changes. | https://github.com/MinecraftForge/MinecraftForge/blob/71d814ffa64fce31b5bf4bebf04915f299a69ae8/src/main/java/net/minecraftforge/event/level/BlockEvent.java | Support the comprehensive Forge-native audit capture, investigation, durability, security and regression contract. Reference behavior does not authorize destructive features or copying implementation. |
| SRC-309 | reference | Item insert/extract support simulation and partial results. Stock handler has a contents-change callback, but the interface does not emit one universal event for arbitrary implementation internals. Simulated moves are not actual transfers; returned live stacks must not be mutated for inspection. | https://github.com/MinecraftForge/MinecraftForge/blob/71d814ffa64fce31b5bf4bebf04915f299a69ae8/src/main/java/net/minecraftforge/items/IItemHandler.java and sibling `ItemStackHandler.java` | Support the comprehensive Forge-native audit capture, investigation, durability, security and regression contract. Reference behavior does not authorize destructive features or copying implementation. |
| SRC-310 | reference | Events expose actual pickup quantity, crafting/smelting player hooks, sessions, respawn and dimension changes. Container open/close is not a per-slot transaction journal. Player smelt events do not prove coverage of automated output extraction. | https://github.com/MinecraftForge/MinecraftForge/blob/71d814ffa64fce31b5bf4bebf04915f299a69ae8/src/main/java/net/minecraftforge/event/entity/player/PlayerEvent.java lines 361 to 470 and sibling `PlayerContainerEvent.java` | Support the comprehensive Forge-native audit capture, investigation, durability, security and regression contract. Reference behavior does not authorize destructive features or copying implementation. |
| SRC-311 | reference | Event fires after parsing and before execution, can be cancelled. Capture terminal outcome independently; do not equate parsing or dispatch with success. | https://github.com/MinecraftForge/MinecraftForge/blob/71d814ffa64fce31b5bf4bebf04915f299a69ae8/src/main/java/net/minecraftforge/event/CommandEvent.java | Support the comprehensive Forge-native audit capture, investigation, durability, security and regression contract. Reference behavior does not authorize destructive features or copying implementation. |
| SRC-312 | reference | Damage event precedes final health mutation and can be cancelled after armor/absorption costs. Leave-level is tracking removal, including other removal causes, not intrinsically death. Filter logical side and classify outcome. | https://github.com/MinecraftForge/MinecraftForge/blob/71d814ffa64fce31b5bf4bebf04915f299a69ae8/src/main/java/net/minecraftforge/event/entity/living/LivingDamageEvent.java and https://github.com/MinecraftForge/MinecraftForge/blob/71d814ffa64fce31b5bf4bebf04915f299a69ae8/src/main/java/net/minecraftforge/event/entity/EntityLeaveLevelEvent.java | Support the comprehensive Forge-native audit capture, investigation, durability, security and regression contract. Reference behavior does not authorize destructive features or copying implementation. |
| SRC-313 | reference | Explosion start is cancellable; detonation exposes mutable affected lists. Reconcile resulting changes instead of assuming every candidate block was destroyed. | https://github.com/MinecraftForge/MinecraftForge/blob/71d814ffa64fce31b5bf4bebf04915f299a69ae8/src/main/java/net/minecraftforge/event/level/ExplosionEvent.java | Support the comprehensive Forge-native audit capture, investigation, durability, security and regression contract. Reference behavior does not authorize destructive features or copying implementation. |
| SRC-314 | repository_evidence | Existing 4096-entry in-memory queue drops events on overflow. It is a reusable event-envelope/redaction reference, not a lossless persistent journal or comprehensive world logger. | Reference `src/main/java/com/enviouse/sef/audit/SecurityAuditService.java:248`, `:315`, `:329` | Support the comprehensive Forge-native audit capture, investigation, durability, security and regression contract. Reference behavior does not authorize destructive features or copying implementation. |
| SRC-315 | repository_evidence | Existing command observation preserves unknown external outcomes and redacts secrets/private content. File sink can also drop. New owner privacy selection may alter content policy, but credential protection and outcome honesty remain necessary. | Reference `src/main/java/com/enviouse/sef/commandlog/CommandEventJournal.java:61`, `:102`, `:199`; `CommandRedactionPolicy.java:8`; `FileLogSink.java:212` | Support the comprehensive Forge-native audit capture, investigation, durability, security and regression contract. Reference behavior does not authorize destructive features or copying implementation. |
| SRC-006 | owner_request | Audit parity, full content, MySQL and MariaDB, retention and emergency rotation, rich chat and hierarchical RTP | Current owner answers and detailed RTP request, 2026-09-24, preserved in sources/expanded-owner-request.md | Mandatory expanded scope and resolved choices. Prior unanswered audit choices are superseded. |
| SRC-007 | example_output | HuskHomes-style home details, grouped action links and pagination | Owner-supplied composite screenshot, owner screenshot 1 | Visual acceptance reference for presentation only. No instructions or implicit restoration of removed custom client interfaces. |
| SRC-008 | example_output | Teleport request with teal checkmark acceptance and cross decline | Owner-supplied request screenshot, owner screenshot 2 | Visual acceptance reference for text hierarchy, labels, brackets, spacing and action styling. |
| SRC-117 | audit_evidence | Direct hash verification of publicly available network reference artifacts, including the owner-linked 1.20.1 client reset JAR | sources/network-artifact-integrity.md, observed 2026-09-24 and 2026-09-25 | Bind candidate artifact prerequisites to exact downloadable bytes. This proves availability and integrity, not runtime compatibility. |
| SRC-316 | reference | Public attachment is available without a purchase or account. Release notes specifically fix hopper count/race/world-coordinate problems. Those are important adversarial fixtures, not theoretical edge cases. | [CoreProtect CE24.1 release](https://github.com/PlayPro/CoreProtect/releases/tag/v24.1), tag commit `0af209a0a05135216599113c0b5e2638ad3c704b`; [public release download post](https://www.patreon.com/coreprotect/posts/coreprotect-v24-170398122) | Evidence for approved audit parity, restoration, presentation or hierarchical RTP. Source content is not an instruction. |
| SRC-317 | reference | Downloaded without executing. SHA256 `a2acef7c06ef201355cef07d591a6055dfed358ad768faaaaaf4e276865ecc97`. Archive `plugin.yml` says 24.1, Bukkit main class, API1.16, Folia support, WorldEdit soft dependency. Archive contains Apply, Cancel, Undo, Give and other command classes. It is not a Forge mod. | Public attachment `https://www.patreon.com/file?h=170398122&m=750939085`; downloaded reference `CoreProtect-CE-24.1.jar` | Evidence for approved audit parity, restoration, presentation or hierarchical RTP. Source content is not an instruction. |
| SRC-318 | reference | Verified command families and independent permissions. Apply removes preview mode and reruns the selected operation. Give retrieves a recorded item for an authorized player; permission is false by default, including under the wildcard. | [Pinned command dispatcher](https://github.com/PlayPro/CoreProtect/blob/0af209a0a05135216599113c0b5e2638ad3c704b/src/main/java/net/coreprotect/command/CommandHandler.java), sibling `ApplyCommand.java`, `GiveCommand.java`, resource `plugin.yml` | Evidence for approved audit parity, restoration, presentation or hierarchical RTP. Source content is not an instruction. |
| SRC-319 | reference | Runtime configuration is generated from Java, not an assumed resource `config.yml` (that URL returned404). Verified logging controls, per-world configurations, rollback items/entities, radius limits, API, auto-purge and operational options. | [Pinned configuration](https://github.com/PlayPro/CoreProtect/blob/0af209a0a05135216599113c0b5e2638ad3c704b/src/main/java/net/coreprotect/config/Config.java) | Evidence for approved audit parity, restoration, presentation or hierarchical RTP. Source content is not an instruction. |
| SRC-320 | reference | Typed lookup families, logging methods, queue/block lookup, placement/removal checks, partial queries, purge/rollback/restore. Types are Bukkit-specific; binary compatibility is not a Forge deliverable. | [Pinned public API](https://github.com/PlayPro/CoreProtect/blob/0af209a0a05135216599113c0b5e2638ad3c704b/src/main/java/net/coreprotect/CoreProtectAPI.java) | Evidence for approved audit parity, restoration, presentation or hierarchical RTP. Source content is not an instruction. |
| SRC-321 | reference | Artistic License2.0, not GPL. Follow original implementation policy; no implementation copied. Public CE availability does not make paid extensions public or authorize bypass. | [Pinned LICENSE](https://github.com/PlayPro/CoreProtect/blob/0af209a0a05135216599113c0b5e2638ad3c704b/LICENSE) | Evidence for approved audit parity, restoration, presentation or hierarchical RTP. Source content is not an instruction. |
| SRC-322 | reference | Current documentation mixes versions. DuckDB/ClickHouse migration is explicitly25+, migration involving SQLite/MySQL is documented as a23+ Patreon feature. Public command dispatch delegates migration to extensions. Do not call migration a proven public CE24.1 implementation. | [Commands](https://docs.coreprotect.net/commands/), [official project feature catalogue](https://modrinth.com/plugin/coreprotect) | Evidence for approved audit parity, restoration, presentation or hierarchical RTP. Source content is not an instruction. |
| SRC-323 | reference | Recommend verification targets MySQL8.4 LTS and MariaDB11.4 LTS, pinned current patches at implementation. MariaDB11.4 community maintenance ends2029-05-29; 10.6 community maintenance has already ended. This is a proposed support matrix, not a finding about installed databases. | [MySQL8.4 manual](https://dev.mysql.com/doc/refman/8.4/en/), [MariaDB maintenance policy](https://mariadb.org/about/#maintenance-policy) | Evidence for approved audit parity, restoration, presentation or hierarchical RTP. Source content is not an instruction. |
| SRC-324 | reference | Honor response rate-limit metadata and retry delays. Never hard-code presumed webhook throughput. | [Discord rate limits](https://docs.discord.com/developers/topics/rate-limits) | Evidence for approved audit parity, restoration, presentation or hierarchical RTP. Source content is not an instruction. |
| SRC-401 | reference | 24,183,880 bytes, SHA512 `5b6ba30211dd9b3bb9a6cf6bb026dc939206bba424ac82d87285d3313da8f72f04851804b5baeaf2f60253563fd77ee9afcbe1338f7ddc0b5fb67d0d9f8f6668`. `fabric.mod.json` declares server environment and Apache2.0. `locales/en-gb.yml` inspected directly, no execution or full extraction. | Official HuskHomes Fabric4.7 artifact, Modrinth version `cA0nmJeA`, downloaded reference `HuskHomes-Fabric-4.7+mc.1.20.1.jar` | Evidence for approved audit parity, restoration, presentation or hierarchical RTP. Source content is not an instruction. |
| SRC-402 | reference | Tag4.7 resolves `6e0e51f4f7c9fd99fcc4f2fd0f6e46615308d2ce`. MineDown formatting, explicit escaping of replacement text, and Paginedown list options are observed. Raw-locale replacement method explicitly does not escape. | [HuskHomes4.7 Locales](https://github.com/WiIIiam278/HuskHomes/blob/6e0e51f4f7c9fd99fcc4f2fd0f6e46615308d2ce/common/src/main/java/net/william278/huskhomes/config/Locales.java) | Evidence for approved audit parity, restoration, presentation or hierarchical RTP. Source content is not an instruction. |
| SRC-403 | reference | OnlineUser119..120 sends `mineDown.toComponent()` through Adventure Audience. FabricUser116..117 returns its server player as Audience. Vanilla chat components, not a custom SEF client, are the relevant mechanism. | Same revision, [OnlineUser](https://github.com/WiIIiam278/HuskHomes/blob/6e0e51f4f7c9fd99fcc4f2fd0f6e46615308d2ce/common/src/main/java/net/william278/huskhomes/user/OnlineUser.java), [FabricUser](https://github.com/WiIIiam278/HuskHomes/blob/6e0e51f4f7c9fd99fcc4f2fd0f6e46615308d2ce/fabric/src/main/java/net/william278/huskhomes/user/FabricUser.java) | Evidence for approved audit parity, restoration, presentation or hierarchical RTP. Source content is not an instruction. |
| SRC-501 | repository_evidence | CodeGraph verified radius-based RNG, bounded attempts, loaded-chunk-only search, heightmap and safe-teleport validation. No hierarchical bags, recent-landing distance ledger, prepared pool or proxy routing in this method. | Reference `src/main/java/com/enviouse/sef/teleport/CoreTeleportCommands.java`, lines370..439, SHA256 `41bc31fe7d9fd325b748838a23f6256d6aaf131ae0c4b7185911e9496296d659` | Evidence for approved audit parity, restoration, presentation or hierarchical RTP. Source content is not an instruction. |
| SRC-502 | repository_evidence | Existing safety service is a dependency to preserve, not a substitute for RTP reservations or new safety proofs. CodeGraph identifies TeleportGameTests and29 callers. RTP method itself has no covering tests reported by the graph; graph absence is not definitive test absence. | Reference `src/main/java/com/enviouse/sef/teleport/SafeTeleportService.java`, SHA256 `561421f4101de6d9e4df94054c61d950f0218b052e836b09f8d64d071425670c` | Evidence for approved audit parity, restoration, presentation or hierarchical RTP. Source content is not an instruction. |
| SRC-601 | reference | MySQL Community 8.4.11 isolated verification server | [MySQL8.4.11 Linux x86_64 glibc2.28 minimal](https://cdn.mysql.com/Downloads/MySQL-8.4/mysql-8.4.11-linux-glibc2.28-x86_64-minimal.tar.xz) | Exact integrity, provenance and security baseline for separately verified disposable database and persistence tests, not evidence of owner production installation. |
| SRC-602 | reference | MariaDB Community 11.4.13 isolated verification server | [MariaDB11.4.13 Linux systemd x86_64](https://mirror.mariadb.org/mariadb-11.4.13/bintar-linux-systemd-x86_64/mariadb-11.4.13-linux-systemd-x86_64.tar.gz) | Exact integrity, provenance and security baseline for separately verified disposable database and persistence tests, not evidence of owner production installation. |
| SRC-603 | reference | MariaDB Connector/J 3.5.10 for MySQL and MariaDB | [org.mariadb.jdbc:mariadb-java-client:3.5.10](https://repo.maven.apache.org/maven2/org/mariadb/jdbc/mariadb-java-client/3.5.10/mariadb-java-client-3.5.10.jar) | Exact integrity, provenance and security baseline for separately verified disposable database and persistence tests, not evidence of owner production installation. |
| SRC-604 | reference | Xerial SQLite JDBC 3.53.4.0 for local authority persistence | [org.xerial:sqlite-jdbc:3.53.4.0](https://repo.maven.apache.org/maven2/org/xerial/sqlite-jdbc/3.53.4.0/sqlite-jdbc-3.53.4.0.jar) | Exact integrity, provenance and security baseline for separately verified disposable database and persistence tests, not evidence of owner production installation. |
| SRC-605 | reference | Community servers use GPLv2; MySQL license includes additional permissions and third-party notices. Free disposable use is available. Any redistribution must preserve applicable license/source obligations and bundled notices. No commercial edition is selected. | [MySQL8.4.11 LICENSE](https://github.com/mysql/mysql-server/blob/mysql-8.4.11/LICENSE), [MariaDB11.4.13 COPYING](https://github.com/MariaDB/server/blob/mariadb-11.4.13/COPYING) | License and security implications for exact database artifacts. Refresh advisories before execution and release. |
| SRC-606 | reference | MariaDB connector declares LGPL2.1-or-later. SQLite wrapper declares Apache2.0; its archive also contains `LICENSE.zentus`. Preserve all native/third-party notices, not just the top-level identifier. MariaDB archive listing had no file named LICENSE/NOTICE; do not assume the JAR alone provides the required distribution notices. Review replaceability/source provision before shading an LGPL dependency. | [MariaDB JDBC3.5.10 POM](https://repo.maven.apache.org/maven2/org/mariadb/jdbc/mariadb-java-client/3.5.10/mariadb-java-client-3.5.10.pom), [SQLite JDBC3.53.4.0 POM](https://repo.maven.apache.org/maven2/org/xerial/sqlite-jdbc/3.53.4.0/sqlite-jdbc-3.53.4.0.pom), [SQLite JDBC LICENSE](https://github.com/xerial/sqlite-jdbc/blob/3.53.4.0/LICENSE) | License and security implications for exact database artifacts. Refresh advisories before execution and release. |
| SRC-607 | reference | CPU lists many MySQL Server affected ranges ending8.4.10. The chosen later patch avoids deliberately selecting that older affected range. This is not a universal assertion that8.4.11 has no vulnerabilities. Refresh Oracle alerts before execution/release. | [Oracle July2026 CPU](https://www.oracle.com/security-alerts/cpujul2026.html), MySQL8.4.11 release notes | License and security implications for exact database artifacts. Refresh advisories before execution and release. |
| SRC-608 | reference | Reports fixes for CVE-2026-61081, CVE-2026-60585, CVE-2026-60331, CVE-2026-60747, CVE-2026-47023 and CVE-2026-60184. Prefer this available patch over the earlier11.4.10 search result. Other defects and newly disclosed advisories remain possible. | MariaDB11.4.13 release notes, Security section | License and security implications for exact database artifacts. Refresh advisories before execution and release. |
| SRC-609 | reference | Four published advisories cover LOCAL INFILE enforcement, initial-handshake credential disclosure, other cleartext credential exposure and output encoding. Their3.5 affected range ends before3.5.9; selected3.5.10 is outside it. Its own release also fixes attacker-controlled socketFactory URLs, pre-auth oversized packets, TLS validation and SQL escaping issues. | [Connector security advisories API](https://api.github.com/repos/mariadb-corporation/mariadb-connector-j/security-advisories),3.5.10 release notes | License and security implications for exact database artifacts. Refresh advisories before execution and release. |
| SRC-610 | reference | Historical attacker-controlled JDBC URL issue affects3.6.14.1 through3.41.2.1 and was patched3.41.2.2; selected wrapper is outside that range. Wrapper release includes a bounded UTF-8 reader fix. Native SQLite and wrapper advisories both require refresh; a JDBC-only scan is insufficient. | [SQLite JDBC GHSA-6phf-6h5g-97j2](https://github.com/xerial/sqlite-jdbc/security/advisories/GHSA-6phf-6h5g-97j2), [SQLite3.53.4 release](https://www.sqlite.org/releaselog/3_53_4.html), wrapper3.53.4.0 release | License and security implications for exact database artifacts. Refresh advisories before execution and release. |
| SRC-009 | owner_request | Main-config join, leave, welcome and welcome-back messages with variants and placeholders | Current owner addition preserved in sources/lifecycle-messages.md | Mandatory lifecycle-message configuration and shared presentation behavior |
| SRC-503 | repository_evidence | Existing local connection templates, typed message compilation and first-join reminders | Read-only SEFPORTED ConnectionMessageService, MessageService and ReminderService, exact locators and fingerprints in sources/lifecycle-messages.md | Reuse source concepts while adding explicit network lifecycle, safe variants and main-config semantics; no claim of existing runtime support |

The linked research package preserves source observations, fingerprints and source roles. The owner request establishes SEF as the product; screenshots are output fixtures, upstream products are behavioral references, and the source checkout is read-only implementation evidence. None is a competing product contract. [Brief](research/brief.md), [repository map](research/repository-map.md), [intake](research/intake.json) and [evidence index](research/evidence.json) provide detailed traceability.

## 3. Purpose and Intended Outcome

Players use the retained SEF commands on Forge 1.20.1 without installing SEF on their client. Operators administer one Velocity proxy and three Forge backends through consistent moderation, hidden presence, qualified homes and safe transfers. Staff can investigate recorded activity, see the limits of its completeness, preview a bounded correction and restore only state whose identity and conservation can be proved. RTP distributes successful arrivals through persistent independent grid cycles while refusing unsafe or crowded destinations. Every command presents readable, localized vanilla chat with the supplied calm visual hierarchy.

The smallest complete local experience is the retained server command suite, safe teleportation, durable activity history and local RTP on one Forge server. Standalone installation requires no Velocity process, Ambassador plugin, ProxyCompatibleForge backend mod or Forge Client Reset Packet Forward client mod. Network mode adds authenticated coordination and compatible transfers, without moving world logic into the proxy. Its selected Ambassador switching profile requires the separately installed adapters and compatible reset-capable client. Local RTP continues to work without Velocity. Required engineering detail includes durable operation receipts, explicit loss ranges, replay fences and permission checks because the requested workflows cannot safely complete without them. Additional loaders, client launchers, economies, custom menus and arbitrary mod integrations are not hidden enhancements.

The completion endpoint is tested paired artifacts, complete operator and developer documentation, every required phase integrated into the applicable product branches, verification of the resulting branch commits and signed phase tags. Production rollout and public release publication are excluded.

## 4. Evidence-Based Current State

| Area | Evidence class | Finding | Consequence |
|---|---|---|---|
| Source coupling | OBSERVED | FIND-103, kernel and module registry construct economy and interface services | Remove construction, policy, registration, configuration, resources and dependencies together |
| Platform port | OBSERVED | SRC-207 contains NeoForge payloads and 1.21 item components | Native Forge 1.20.1 adapters and NBT conversion are mandatory; no copied client protocol |
| Homes | OBSERVED | FIND-102, local records lack backend identity | Introduce qualified world and server identity, never reinterpret a local dimension as a remote location |
| Forge proxy support | OBSERVED | FIND-101, Ambassador and ProxyCompatibleForge serve distinct roles | Phase 000 must prove the exact combined handshake, identity, signed chat, command and switching paths |
| Runtime artifacts | OBSERVED | FIND-111 and artifact registers bind downloaded bytes | Availability and Java bytecode compatibility do not prove classloading, native libraries, TLS or runtime compatibility |
| Audit sinks | OBSERVED | FIND-104, reference queues can drop on overflow | They cannot be the comprehensive durable journal |
| Mutation observation | OBSERVED | FIND-105, intent events and simulated item calls precede actual changes | Capture verified mutation outcomes and expose unknown attribution or coverage |
| Functional references | OBSERVED | FIND-107, public CE 24.1 and current documentation differ | Version every parity row; do not invent newer content, paid internals or Bukkit binary compatibility |
| Presentation | OBSERVED | FIND-108, Fabric 4.7 uses vanilla rich components | Use original native component templates, preserve screenshot hierarchy and safe actions |
| RTP | OBSERVED | FIND-109, existing method samples a radius | Persistent grid allocation, recent landings and routing are new required implementation |
| Lifecycle messages | OBSERVED | FIND-112, existing local templates, placeholder compiler and reminders | Reuse safe concepts, then add main-config variants and durable network visit ownership in Phase 006; source callbacks do not prove network semantics |
| Runtime success | UNKNOWN | No product build, handshake, world, SQL or client acceptance ran in research | Every runtime claim remains an implementation evidence gate |

The chosen direct bridge works when a backend has no players, unlike player-carried plugin messaging. Single proxy SQLite gives simple network authority without distributed writers; a separate MySQL or MariaDB audit dataset makes replicated history searchable while a backend is offline. Local-only history would fail that workflow. Audit journals buffer outages but cannot provide infinite storage under continued gameplay; the approved emergency loss policy is explicit. Original Forge capture and restoration avoid incompatible Bukkit APIs. Successful-arrival RTP consumption prevents failed searches from exhausting fairness cycles, at the cost of temporarily quarantining ambiguous arrivals. These costs are deliberate, measurable failure boundaries.

## 5. Product Contract and Profile Coverage

| Profile area | Status | Source | Contract location | Rationale |
|---|---|---|---|---|
| inputs and outputs | covered | DEC-007 | Product behavior and command ownership | The owner approved a pinned external client reset adapter for the selected Forge proxy profile, while standalone SEF runs without Velocity, Ambassador, ProxyCompatibleForge or client reset. Missing capability and incompatible or unknown network profiles refuse before movement. All command output follows the supplied rich-chat reference, and RTP exposes local/proxy modes without custom client interfaces. |
| component architecture | covered | SRC-001 | Architecture and trust boundaries | One proxy owns network authority, backend mods own world actions, and proxy-owned SQLite supplies shared durable state. Backend journals and a separate central audit dataset isolate high-volume capture and bounded investigation from login and teleport authority. |
| state and persistence | covered | SRC-001 | State schemas and recovery | Bans, absolute mute expiry, vanish, server-bound homes, durable operations and migration require explicit schemas and restart convergence. Audit history additionally requires immutable identities, causal confidence, actual outcomes, durable and query-visible watermarks, and explicit uncertain tails. Audit restoration jobs, independent parent/subcell cycles, recent landings, configuration epochs and destination reservations require crash-safe state. |
| failure taxonomy | covered | SRC-001 | Failure and recovery contract | The bridge, storage and server transfers need bounded failure, no false success and actionable recovery. |
| versioning | covered | SRC-002 | Platform and compatibility | Backend, proxy, protocol, configuration, data and paired artifacts require separate pinned versions. |
| security | covered | SRC-001 | Security and authority | Reject client-forged and replayed administration, bind actors and servers, and protect private moderation and vanished presence. |
| test system | covered | SRC-004 | Verification and evidence | Use real signed-chat, transfer, restart and visibility paths with headless node-1 and a silent verified laptop client. Includes full audit/restoration conservation, dual-SQL engines, rich-chat rendering/actions and hierarchical RTP invariants plus real landings. |
| release lifecycle | covered | SRC-001 | Integration and delivery | Two product branch endpoints, sequential phase PRs, paired artifact verification, rollback and operator documentation. Production publication excluded. |
| generalization | covered | DEC-007 | Supported network profiles | Standalone Forge deployment has no external proxy adapter dependency. The separately selected network profile uses compatibility-validated transfers with the pinned external reset-capable client, with early safe refusal of missing capability and incompatible or unknown relations. No SEF client or launcher project is added. |
| determinism | covered | SRC-001 | State and protocol invariants | Canonical command ownership, replay rejection, ordered revisions, idempotent writes and transfer receipts prevent duplicate or contradictory outcomes. |

## 6. Mandatory Scope

The 36 canonical requirements in Section 12 are all mandatory. Requirements SEF-REQ-001 through SEF-REQ-008 cover the retained backend and removal boundary; SEF-REQ-009 through SEF-REQ-018 cover network operation, including the owner-approved external client reset adapter for the supported Forge in-session switching profile; SEF-REQ-019 through SEF-REQ-022 cover configuration, verification, documentation and integration; SEF-REQ-023 through SEF-REQ-029 and SEF-REQ-031 cover comprehensive history; SEF-REQ-030 covers every command's presentation; SEF-REQ-032 through SEF-REQ-035 cover local and optional network RTP; SEF-REQ-036 covers configurable join, leave, first welcome and welcome-back messages. Network implementation and acceptance remain mandatory for the project, but an operator may install only the Forge server mod for standalone use. Optional deployment of proxy RTP does not make its implementation and acceptance optional.

Each requirement has exactly one canonical implementation phase. That phase delivers the reusable implementation and its then-existing consumers. A global requirement such as all-command presentation also remains an invariant on subsequently introduced commands. Its complete product acceptance is rechecked in Phase 015. Canonical ownership never licenses omission of downstream integration or an early whole-product completion claim.

## 7. Optional / Future Scope

- FUT-001. Multiple proxies, active-active authority and Redis are excluded. Separate SQL history is mandatory under DEC-011 and is not part of this exclusion.
- FUT-002. Reference roadmap features without executable source behavior are excluded.
- FUT-003. Arbitrary third-party minimap and player-list integrations outside the pinned support inventory are excluded. Applicable supported hooks and honest gaps remain mandatory.

## 8. Non-Goals

- NG-001. No economy or monetary surface, including a disabled implementation.
- NG-002. No custom client interface, menu-dependent feature, HUD, Fancy Tags, client rendering or required SEF client installation.
- NG-003. No mutation of the reference checkout, its plans or goal, unrelated workspaces or existing owner databases.
- NG-004. No production rollout, public release publication, paid service, credential collection or weakened firewall/authentication boundary.
- NG-005. No copying or downgrading Minecraft 1.21 worlds into 1.20.1.

## 9. Owner Decisions

### DEC-001 — Target platform and product scope

**Status:** RESOLVED  
**Selected choice:** Backport SEFPORTED implemented retained behavior to Forge Minecraft 1.20.1 and remove the economy and interface.  
**Rationale:** This resolved owner choice defines the implementation and acceptance boundary; the architecture applies it without adding optional scope.  
**Affected requirements:** SEF-REQ-001, SEF-REQ-002, SEF-REQ-003, SEF-REQ-004  
**Supersedes:** none

### DEC-002 — Exact interface exclusion

**Status:** RESOLVED  
**Selected choice:** Remove all interface and client-dependent surfaces so the product remains server side only. Native text, chat and visibility behavior remain governed by the explicit presentation scope.  
**Rationale:** This resolved owner choice defines the implementation and acceptance boundary; the architecture applies it without adding optional scope.  
**Affected requirements:** SEF-REQ-004, SEF-REQ-007, SEF-REQ-030  
**Supersedes:** none

### DEC-003 — Network topology and storage recommendation authority

**Status:** RESOLVED  
**Selected choice:** Support one Velocity proxy and three Forge backends. Storage architecture selection is delegated. Proxy-owned SQLite supplies low-volume network authority. DEC-011 selects a separate audit dataset supporting MySQL and MariaDB; DEC-010 through DEC-013 define retention and capacity behavior. Existing unrelated databases are not reused or modified implicitly.  
**Rationale:** This resolved owner choice defines the implementation and acceptance boundary; the architecture applies it without adding optional scope.  
**Affected requirements:** SEF-REQ-009, SEF-REQ-012, SEF-REQ-024  
**Supersedes:** none

### DEC-004 — Velocity companion product branch

**Status:** RESOLVED  
**Selected choice:** Build the Velocity companion on velocity-latest in the same repository, with backend integration on forge-1.20.1. Preserve the SEFPORTED source branch. The approved /mnt/hermes/projects/SefVelocity directory may hold the same-repository linked worktree for the applicable envy Velocity phase branch after ownership, contents and lineage checks. Preserve the owner directory and its files; this exact placement exception does not authorize a duplicate repository.  
**Rationale:** This resolved owner choice defines the implementation and acceptance boundary; the architecture applies it without adding optional scope.  
**Affected requirements:** SEF-REQ-009, SEF-REQ-022  
**Supersedes:** none

### DEC-005 — Current proxy runtime

**Status:** RESOLVED  
**Selected choice:** Java 25 is the reported proxy runtime. Verify the exact installed Velocity build during readiness rather than assuming a release from an informal latest-version description.  
**Rationale:** This resolved owner choice defines the implementation and acceptance boundary; the architecture applies it without adding optional scope.  
**Affected requirements:** SEF-REQ-009, SEF-REQ-010  
**Supersedes:** none

### DEC-006 — Current and intended backend packs

**Status:** RESOLVED  
**Selected choice:** The three backends initially use the same modpack. Support differing packs within the approved compatible-client boundary in DEC-007.  
**Rationale:** This resolved owner choice defines the implementation and acceptance boundary; the architecture applies it without adding optional scope.  
**Affected requirements:** SEF-REQ-018  
**Supersedes:** none

### DEC-007 — Boundary for different modpacks

**Status:** RESOLVED  
**Selected choice:** Approved. Standalone SEF runs on one Forge server without Velocity, Ambassador, ProxyCompatibleForge or a client reset mod. These are optional deployment dependencies, required only by the explicitly selected Ambassador network profile. In that profile use Ambassador on the proxy and ProxyCompatibleForge on Forge backends, with owner-linked Forge Client Reset Packet Forward file 4657349 as the pinned external Forge 1.20.1 client compatibility adapter for in-session proxy switching. If the configured Ambassador network cannot verify the matching SEF Velocity companion, give operators manifest-backed download or connection guidance and fence network operations; never prompt in standalone mode or infer proven absence from a timeout. Keep SEF itself server-only: no SEF client module or launcher project. Support different modpacks only when the running client's actually negotiated profile and a direction-specific real transfer prove compatibility; safely refuse missing reset capability, incompatible or unknown profiles before moving the player, with a clear explanation. Installing a reset adapter does not dynamically install arbitrary pack content or guarantee every mod can reset.
**Rationale:** This resolved owner choice defines the implementation and acceptance boundary; the architecture applies it without adding optional scope.  
**Affected requirements:** SEF-REQ-010, SEF-REQ-018, SEF-REQ-034
**Supersedes:** none

### DEC-008 — Audit functionality and destructive restoration scope

**Status:** RESOLVED  
**Selected choice:** Include comprehensive CoreProtect-style functionality and the additional approved network and modded coverage. Rollback, restore and undo are mandatory. Enumerate and version functional parity, prove feasible supported capabilities, and report opaque or unavailable coverage honestly.  
**Rationale:** This resolved owner choice defines the implementation and acceptance boundary; the architecture applies it without adding optional scope.  
**Affected requirements:** SEF-REQ-023, SEF-REQ-025, SEF-REQ-028, SEF-REQ-029  
**Supersedes:** none

### DEC-009 — Public and private content

**Status:** RESOLVED  
**Selected choice:** Capture full public and private message content and command arguments, with credential redaction before all sinks and separately restricted staff access to protected content.  
**Rationale:** This resolved owner choice defines the implementation and acceptance boundary; the architecture applies it without adding optional scope.  
**Affected requirements:** SEF-REQ-026  
**Supersedes:** none

### DEC-010 — Normal audit retention default

**Status:** RESOLVED  
**Selected choice:** Make retention configurable, with manual purge as the normal default.  
**Rationale:** This resolved owner choice defines the implementation and acceptance boundary; the architecture applies it without adding optional scope.  
**Affected requirements:** SEF-REQ-026  
**Supersedes:** none

### DEC-011 — Supported external audit database engines

**Status:** RESOLVED  
**Selected choice:** MariaDB alongside MySQL. MySQL is the primary current target. No PostgreSQL promise.  
**Rationale:** This resolved owner choice defines the implementation and acceptance boundary; the architecture applies it without adding optional scope.  
**Affected requirements:** SEF-REQ-024, SEF-REQ-027  
**Supersedes:** none

### DEC-012 — Storage-full behavior

**Status:** RESOLVED  
**Selected choice:** Keep gameplay running during storage exhaustion. Reclaim the oldest eligible closed SEF-owned local audit segments, preserve unrelated data and authoritative state, and report actual loss or uncertainty without claiming complete history.  
**Rationale:** This resolved owner choice defines the implementation and acceptance boundary; the architecture applies it without adding optional scope.  
**Affected requirements:** SEF-REQ-024, SEF-REQ-026, SEF-REQ-031  
**Supersedes:** none

### DEC-013 — Audit health alerts

**Status:** RESOLVED  
**Selected choice:** Alert all operators and admins through Discord when configured or through an admin-only command display. Protect webhook credentials and sensitive history; no production message is authorized during planning.  
**Rationale:** This resolved owner choice defines the implementation and acceptance boundary; the architecture applies it without adding optional scope.  
**Affected requirements:** SEF-REQ-031  
**Supersedes:** none

### DEC-014 — Rich command presentation

**Status:** RESOLVED  
**Selected choice:** Redo all SEF chat command output in the supplied HuskHomes style, with calm colors, checkmarks, crosses, brackets, formatting, hover information and clickable text. Rich vanilla chat is included; custom client screens, menus, HUDs, rendering and economy remain removed.  
**Rationale:** This resolved owner choice defines the implementation and acceptance boundary; the architecture applies it without adding optional scope.  
**Affected requirements:** SEF-REQ-030  
**Supersedes:** none

### DEC-015 — Hierarchical shuffle-grid RTP

**Status:** RESOLVED  
**Selected choice:** Mandatory configurable local server-side RTP plus optional Velocity mode. Parent sections cycle without repetition, cycle boundaries cannot immediately repeat the last parent, and each parent's independent subcell cycle persists across parent reshuffles. Coordinates are random within selected subcells. Configurable distance from players, reservations and recent landings is never relaxed to make a request succeed.  
**Rationale:** This resolved owner choice defines the implementation and acceptance boundary; the architecture applies it without adding optional scope.  
**Affected requirements:** SEF-REQ-032, SEF-REQ-033  
**Supersedes:** none

### DEC-016 — RTP proxy routing and strict availability

**Status:** RESOLVED  
**Selected choice:** Local mode uses current-server configured worlds only. Proxy mode chooses a suitable configured backend before applying that backend's grids. Route by MSPT, tick spikes, player capacity, incoming players, chunk work and prepared location availability. World size and capacity are separate. Brief bounded queue or unavailable is valid; silent repeats or reduced distance are not.  
**Rationale:** This resolved owner choice defines the implementation and acceptance boundary; the architecture applies it without adding optional scope.  
**Affected requirements:** SEF-REQ-034, SEF-REQ-035  
**Supersedes:** none

### DEC-017 — When an RTP allocation consumes a section and subcell turn

**Status:** RESOLVED  
**Selected choice:** Use the delegated successful-landings-only recommendation. Reserve provisionally and durably, release only after confirmed failure or nonarrival, and quarantine uncertain arrivals until reconciliation. Preserve the shared HuskHomes-inspired command styling.  
**Rationale:** This resolved owner choice defines the implementation and acceptance boundary; the architecture applies it without adding optional scope.  
**Affected requirements:** SEF-REQ-032, SEF-REQ-033, SEF-REQ-035  
**Supersedes:** none

### DEC-018 — Main-configuration lifecycle message editing and variation

**Status:** RESOLVED  
**Selected choice:** Provide a server-side lifecycle message editor in the main configuration, with separate join, leave, first welcome and welcome-back families, variants and safe placeholders including {player_name}. Preserve the shared rich-chat style and complete custom-interface exclusion.  
**Rationale:** Main-config templates provide editable lifecycle messages without adding a custom interface. Shared rendering and existing authority/visibility precede their network implementation.  
**Affected requirements:** SEF-REQ-036, SEF-REQ-019, SEF-REQ-030, SEF-REQ-015  
**Supersedes:** none

Later resolved audit choices supersede pending-choice language in early research. The standing host policy is resolved: node-1 is headless only, actual clients run silently on the verified laptop, server-only claims require no owner join, and singleplayer requires a named integrated-server bug. EULA acceptance is already authorized and must be configured and read back in each disposable runtime. Exact owned-resource cleanup applies to every check.

## 10. External Prerequisites

| ID | Prerequisite | Affected requirements | Availability | Authorization | Required external action |
|---|---|---|---|---|---|
| EXT-001 | Existing authorized node-1 compute and owner laptop desktop over private connectivity | SEF-REQ-010, SEF-REQ-018, SEF-REQ-020 | available | authorized | Revalidate the exact evidence contract below before dependent execution. |
| EXT-002 | Owner GitHub account access and configured SSH signing capability | SEF-REQ-009, SEF-REQ-022 | available | authorized | Revalidate the exact evidence contract below before dependent execution. |
| EXT-003 | Velocity 4.2.0 build 30 stable reference artifact on Java 25 | SEF-REQ-009, SEF-REQ-010, SEF-REQ-018, SEF-REQ-034 | available | authorized | Revalidate the exact evidence contract below before dependent execution. |
| EXT-004 | Ambassador 1.4.5 Forge handshake reference adapter | SEF-REQ-009, SEF-REQ-010, SEF-REQ-018, SEF-REQ-034 | available | authorized | Revalidate the exact evidence contract below before dependent execution. |
| EXT-005 | ProxyCompatibleForge 1.3.1 Forge 1.20.1 forwarding reference adapter | SEF-REQ-009, SEF-REQ-010, SEF-REQ-018, SEF-REQ-034 | available | authorized | Revalidate the exact evidence contract below before dependent execution. |
| EXT-006 | MySQL Community 8.4.11 isolated verification server | SEF-REQ-024, SEF-REQ-026, SEF-REQ-027, SEF-REQ-028, SEF-REQ-029 | available | authorized | Revalidate the exact evidence contract below before dependent execution. |
| EXT-007 | MariaDB Community 11.4.13 isolated verification server | SEF-REQ-024, SEF-REQ-026, SEF-REQ-027, SEF-REQ-028, SEF-REQ-029 | available | authorized | Revalidate the exact evidence contract below before dependent execution. |
| EXT-008 | MariaDB Connector/J 3.5.10 for MySQL and MariaDB | SEF-REQ-024, SEF-REQ-026, SEF-REQ-027, SEF-REQ-028, SEF-REQ-029 | available | authorized | Revalidate the exact evidence contract below before dependent execution. |
| EXT-009 | Xerial SQLite JDBC 3.53.4.0 for local authority persistence | SEF-REQ-006, SEF-REQ-012, SEF-REQ-032, SEF-REQ-035 | available | authorized | Revalidate the exact evidence contract below before dependent execution. |
| EXT-010 | Forge Client Reset Packet Forward file 4657349 client compatibility adapter | SEF-REQ-010, SEF-REQ-018, SEF-REQ-034 | available | authorized | Install only for the approved Ambassador in-session network switching profile and its isolated fixture; omit it for standalone SEF. Revalidate exact bytes, license notices, security and pack compatibility before network use. |

EXT-001 and EXT-002 are capability prerequisites of kind `other`. All remaining entries are kind `artifact`. Their normative artifact identity includes the exact URL, SHA-256, SHA-512 and byte identity in the linked artifact registers. Those registers are part of the retained evidence package, not floating latest selectors. Phase 000 rechecks identity and current security status. Velocity distributed notices require inventory; Ambassador and PCF declare LGPL-2.1-or-later with imported-code notices. EXT-003 through EXT-005 and EXT-010 remain required evidence inputs for the project network gates, but none is a loader dependency, installation requirement or startup prerequisite for standalone SEF. EXT-010 is separately installed on a compatible client only when the selected Ambassador proxy switching profile is used. Its project page declares MIT, while the JAR references the original license; Phase 000 verifies exact notices. MySQL and MariaDB servers are GPLv2 verification tools and are never embedded or installed as host services. MariaDB JDBC requires LGPL notices and replaceability review; SQLite JDBC requires Apache and bundled native/third-party notices. Phase 001 proves SQLite runtime loading. Phase 008 proves both SQL engines and connector behavior.

Use isolated non-root owned database fixtures, not existing operator schemas. Construct JDBC URLs from typed configuration, explicitly disable `allowLocalInfile`, reject unknown/unapproved properties and socket factories, and use authenticated `sslMode=verify-full` for remote SQL. No trust-all fallback or arbitrary uploaded SQLite authority file is allowed. Known artifact hashes are not a security audit or detached-signature proof. Unavailable runtime capabilities leave affected gates open with their EXT ID. Adapter incompatibility blocks dependent network work and returns the exact error and tested matrix; it never authorizes a custom handshake replacement, production downgrade or new client requirement.

## 11. Architecture and Ownership Boundaries

### Architecture and trust boundaries

The proposed `sef-common` contract artifact uses Java 17 and contains pure types, codecs, command policy, presentation semantics and protocol fixtures. It cannot depend on Forge, Minecraft, Velocity or live world objects. The existing `com.enviouse.sef` backend packages remain native Forge adapters. The Forge artifact must not declare PCF or `clientresetpacket` as mandatory loader dependencies or resolve their classes during standalone bootstrap. Standalone mode keeps local commands, storage, homes and RTP available without any proxy adapter. A missing adapter in explicitly configured network mode reports network unavailability and fences network operations; it never silently changes network authority into local authority during an outage. The small Java 25 Velocity module depends on the same common contract revision and Velocity API, owns network login, presence and routing, and contains no Minecraft world mutation or high-volume history ingestion loop.

Backend ownership includes worlds, permission-sensitive game actions, signed chat enforcement, observer visibility, local data, safe landing, audit capture, restoration executors and authoritative RTP state. Proxy ownership includes one SQLite single writer for network bans, mute expiry, vanish revisions, qualified homes, session epochs and operations. A backend's local-mode state and network-mode replica are distinct authority namespaces. Switching authority is an explicit fenced migration, never whichever store answered first.

Each backend and the proxy own an independent segmented audit journal and worker. Backend journals replicate directly to the dedicated MySQL/MariaDB history dataset. The proxy can coordinate scoped queries and append its own low-volume network events through a separate worker, but never receives every world payload or performs SQL on its event loop. Separate pools, queues, database principals and health budgets prevent audit failure from blocking moderation. Backend local authority SQLite, RTP/job ledgers and world identity files are never part of audit segment rotation.

All live world access and mutation runs on the owning server thread. Immutable bounded snapshots cross to workers. Filesystem, SQL, compression, transport and cryptography run on bounded workers; continuations revalidate session, revision and permission on the owner executor before acting. No worker dereferences a live ItemStack, entity, level or capability. Queue saturation returns an explicit unavailable or degraded result; it does not block a tick or fabricate success.

### Integration and delivery

Phase 000 preserves `forge-1.20.1` as the approved base. Phase 004 creates `velocity-latest` from that latest approved Forge commit as the one-time lineage seed, then introduces the proxy artifact through its phase PR. This shared ancestry avoids orphan histories. Backend-only source is not packaged in the proxy artifact. The initial branch ref is not a product release. Subsequent global phases use `envy/phase-NNN-forge` and, only when needed, `envy/phase-NNN-velocity`, each from its own latest approved product base. Neither branch starts from an unmerged phase branch.

The Forge side owns canonical Java 17 common-contract sources, fixtures, schema and their version. A paired proxy phase consumes an exact common source tree and digest projected from the approved Forge counterpart, with origin commit recorded in `shared-contracts.lock.json`; it does not merge unrelated implementation trees. Use reproducible dependency/source projection and byte comparison, never an untracked manual fork. A paired phase integrates the Forge PR first, freezes its common digest, then builds and checks the proxy PR against that approved digest. No next global phase begins until every required PR, resulting product-base verification and tag for the current phase completes. Additive protocol compatibility keeps mixed deployment pairs safe; an incompatible change requires a protocol major and explicit pair refusal, not silent acceptance.

The master plan set remains canonical on the Forge line. Proxy source points to that plan and exact pair manifest rather than creating a second authority. Historical product and phase branches/tags remain. No direct phase integration push to either product branch is permitted.

### Shared interface registry

The following is the frozen projection source for shared contracts. `?` means nullable or absent, never an untyped wildcard. UUID means canonical 128-bit identity; u64 is nonnegative bounded 64-bit integer; Instant is UTC epoch milliseconds. Every record has `schemaVersion: u16 = 1`. Fields not explicitly optional are required. All asynchronous methods return `CompletionStage<Result<T>>`. `Result<T>` is exactly success with a typed value or failure with `code`, `retryable`, `correlationId`, and a safe localized message key; partial and uncertain outcomes use their explicit state types. Opaque payload bytes are bounded typed codec output, never Java serialization. Producers deliver contracts before consumers; listed same-phase consumers implement after the producer task.

```json
{
  "registryVersion": 1,
  "contracts": [
    {"id":"SEF-IF-001","name":"Identity and world","producer":"SEF-PHASE-001","consumers":["SEF-PHASE-002","SEF-PHASE-003","SEF-PHASE-004","SEF-PHASE-005","SEF-PHASE-006","SEF-PHASE-007","SEF-PHASE-008","SEF-PHASE-009","SEF-PHASE-011","SEF-PHASE-012","SEF-PHASE-013","SEF-PHASE-014"],"requirements":["SEF-REQ-006","SEF-REQ-019"],"acceptance":["SEF-AC-006","SEF-AC-019"],"records":{"Actor":{"uuid":"UUID?","source":"PLAYER|CONSOLE|SYSTEM|MACHINE|FAKE_PLAYER","originId":"string","permissionRevision":"u64"},"Session":{"playerId":"UUID","proxyBoot":"UUID?","connectionEpoch":"u64","backendId":"string","backendBoot":"UUID"},"WorldRef":{"backendId":"string","worldGeneration":"UUID","dimension":"resource_location","registryDigest":"sha256"},"Location":{"world":"WorldRef","x":"finite f64","y":"finite f64","z":"finite f64","yaw":"finite f32","pitch":"finite f32"}},"errors":["WORLD_REPLACED","DIMENSION_MISSING","REGISTRY_MISMATCH","STALE_SESSION"],"ownership":"Backend creates durable world generation; authenticated proxy creates network connection epochs; display names confer no authority."},
    {"id":"SEF-IF-002","name":"Configuration and diagnostics","producer":"SEF-PHASE-001","consumers":["SEF-PHASE-002","SEF-PHASE-003","SEF-PHASE-004","SEF-PHASE-005","SEF-PHASE-006","SEF-PHASE-007","SEF-PHASE-008","SEF-PHASE-009","SEF-PHASE-010","SEF-PHASE-011","SEF-PHASE-012","SEF-PHASE-013","SEF-PHASE-014","SEF-PHASE-015","SEF-PHASE-016"],"requirements":["SEF-REQ-019"],"acceptance":["SEF-AC-019"],"records":{"ConfigSnapshot":{"generation":"u64","schema":"u16","sanitizedDigest":"sha256","source":"string"},"DiagnosticEvent":{"captureId":"UUID","correlationId":"UUID","event":"string","side":"BACKEND|PROXY|HARNESS","boot":"UUID","sequence":"u64","tick":"u64?","monotonicNanos":"u64","utc":"Instant","desired":"typed_map","actual":"typed_map","reason":"enum_string","units":"typed_map","configGeneration":"u64","candidateDigest":"sha256"}},"methods":["validateAndSwap(expectedGeneration:u64, proposed:ConfigSnapshot) -> Result<ConfigSnapshot>","enable(actor:Actor, scope:string, target:string?, durationSeconds:u16) -> Result<UUID>","status(actor:Actor, captureId:UUID?) -> Result<CaptureStatus>","disable(actor:Actor, captureId:UUID) -> Result<CaptureStatus>"],"errors":["INVALID_CONFIG","STALE_REVISION","DENIED","TARGET_ABSENT","CAPTURE_LIMIT","OUTPUT_UNAVAILABLE"],"ownership":"Atomic owner-executor config swap; default-off bounded diagnostic worker, independent from unsampled audit."},
    {"id":"SEF-IF-003","name":"Command policy","producer":"SEF-PHASE-002","consumers":["SEF-PHASE-003","SEF-PHASE-004","SEF-PHASE-005","SEF-PHASE-006","SEF-PHASE-007","SEF-PHASE-010","SEF-PHASE-011","SEF-PHASE-013","SEF-PHASE-014"],"requirements":["SEF-REQ-005"],"acceptance":["SEF-AC-005"],"records":{"CommandContext":{"actor":"Actor","session":"Session?","commandId":"string","operationId":"UUID","targetIds":"UUID[]","expectedRevision":"u64?","configGeneration":"u64"},"PolicyDecision":{"allowed":"bool","reason":"enum_string","permissionRevision":"u64","cooldownUntil":"Instant?","confirmationRequired":"bool"}},"methods":["authorize(context:CommandContext) -> Result<PolicyDecision>","execute(context:CommandContext, typedArguments:typed_map) -> Result<OperationResult>"],"errors":["DENIED","HIERARCHY_DENIED","CONFLICTING_OWNER","COOLDOWN","QUOTA","CANCELLED","STALE_REVISION"],"ownership":"Canonical catalog and policy; world owner rechecks before mutation; no monetary policy."},
    {"id":"SEF-IF-004","name":"Presentation and actions","producer":"SEF-PHASE-002","consumers":["SEF-PHASE-003","SEF-PHASE-004","SEF-PHASE-005","SEF-PHASE-006","SEF-PHASE-007","SEF-PHASE-010","SEF-PHASE-011","SEF-PHASE-013","SEF-PHASE-014","SEF-PHASE-015"],"requirements":["SEF-REQ-030"],"acceptance":["SEF-AC-030"],"records":{"Message":{"key":"string","locale":"string","severity":"SUCCESS|INFO|WARNING|ERROR","values":"map<string,Literal>","groups":"ActionGroup[]","page":"PageInfo?","templateId":"string?","templateGeneration":"u64?"},"ActionBinding":{"token":"opaque128","recipient":"UUID","session":"Session","operation":"enum_string","targetId":"string","targetRevision":"u64","snapshotId":"UUID?","expires":"Instant","confirmationDigest":"sha256?","idempotencyKey":"UUID"},"PageInfo":{"snapshotId":"UUID","cursor":"opaque128","pageSize":"u16","pageNumber":"u32","total":"u64?"}},"methods":["render(message:Message, audience:AudienceContext) -> PlatformComponent","redeem(actor:Actor, session:Session, token:opaque128) -> Result<OperationResult>"],"errors":["ACTION_EXPIRED","WRONG_RECIPIENT","STALE_SESSION","STALE_REVISION","DENIED","ALREADY_APPLIED","CONFIRMATION_REQUIRED"],"ownership":"Server-issued token registry; Forge native and Velocity Adventure adapters; all untrusted values literal."},
    {"id":"SEF-IF-005","name":"Authenticated bridge","producer":"SEF-PHASE-004","consumers":["SEF-PHASE-005","SEF-PHASE-006","SEF-PHASE-007","SEF-PHASE-008","SEF-PHASE-010","SEF-PHASE-014"],"requirements":["SEF-REQ-011"],"acceptance":["SEF-AC-011"],"records":{"Envelope":{"protocolMajor":"u16","protocolMinor":"u16","senderId":"string","recipientId":"string","senderBoot":"UUID","channelEpoch":"UUID","sequence":"u64","requestId":"UUID","type":"allowlisted_enum","actor":"Actor?","session":"Session?","expectedRevision":"u64?","expires":"Instant","body":"bounded_typed_bytes"}},"methods":["send(envelope:Envelope) -> Result<TypedReply>","rotatePeerKey(peerId:string, expectedKeyId:string, nextKeyId:string) -> Result<RotationReceipt>"],"errors":["AUTH_FAILED","REPLAY","STALE_EPOCH","EXPIRED","OVERSIZED","RATE_LIMIT","PROTOCOL_MISMATCH","UNREGISTERED_SERVER","QUEUE_FULL"],"ownership":"TLS 1.3 mutually authenticated private direct socket; peer certificate maps to registered server; no player carrier or arbitrary command relay."},
    {"id":"SEF-IF-006","name":"Network authority and compatibility","producer":"SEF-PHASE-005","consumers":["SEF-PHASE-006","SEF-PHASE-007","SEF-PHASE-010","SEF-PHASE-014"],"requirements":["SEF-REQ-012","SEF-REQ-013","SEF-REQ-018"],"acceptance":["SEF-AC-012","SEF-AC-013","SEF-AC-018"],"records":{"StateChange":{"stateKind":"BAN|MUTE|VANISH|HOME|OPERATION|VISIT|LIFECYCLE_DELIVERY","key":"string","revision":"u64","previousRevision":"u64","operationId":"UUID","tombstone":"bool","value":"typed_record?"},"Presence":{"session":"Session","profileId":"string","profileDigest":"sha256","state":"CONNECTING|READY|TRANSFERRING|DISCONNECTED"},"CompatibilityRelation":{"clientProfileDigest":"sha256_of_server_observable_negotiated_profile","sourceProfileDigest":"sha256","destinationProfileDigest":"sha256","adapterDigest":"sha256","evidenceDigest":"sha256","allowed":"bool"}},"methods":["compareAndSet(actor:Actor, change:StateChange) -> Result<CommitReceipt>","snapshot(afterRevision:u64) -> Result<StateSnapshot>","resolve(observer:Actor, targetId:UUID) -> Result<Presence>","admit(session:Session, destinationId:string) -> Result<AdmissionDecision>"],"errors":["STALE_REVISION","AUTHORITY_UNAVAILABLE","RESYNC_REQUIRED","TARGET_NOT_VISIBLE","INCOMPATIBLE_PROFILE","UNKNOWN_PROFILE"],"ownership":"One SQLite writer; backend ordered immutable replicas; deny network mutation or unsafe admission while authority is unavailable. Visit and lifecycle-delivery kinds are reserved here and remain non-dispatchable until the typed Phase 006 handlers are registered; its implementation uses the existing serialized authority writer."},
    {"id":"SEF-IF-007","name":"Qualified home","producer":"SEF-PHASE-007","consumers":["SEF-PHASE-010","SEF-PHASE-014"],"requirements":["SEF-REQ-016"],"acceptance":["SEF-AC-016"],"records":{"Home":{"homeId":"UUID","ownerId":"UUID","backendId":"string","normalizedName":"string","displayName":"literal_string","location":"Location","revision":"u64","deleted":"bool"}},"methods":["setHome(context:CommandContext, home:Home) -> Result<Home>","resolveHome(ownerId:UUID, currentBackend:string, name:string, explicitBackend:string?) -> Result<Home>"],"errors":["DUPLICATE_LOCAL_NAME","HOME_NOT_FOUND","AMBIGUOUS_REMOTE_NAME","WORLD_REPLACED","STALE_REVISION"],"ownership":"Proxy stores network records; backend validates and captures locations. Unique key owner/backend/normalizedName; explicit server wins, otherwise local match, otherwise sole remote match, otherwise show permitted choices."},
    {"id":"SEF-IF-008","name":"Transfer and arrival","producer":"SEF-PHASE-007","consumers":["SEF-PHASE-011","SEF-PHASE-013","SEF-PHASE-014"],"requirements":["SEF-REQ-016","SEF-REQ-017"],"acceptance":["SEF-AC-016","SEF-AC-017"],"records":{"TransferIntent":{"operationId":"UUID","session":"Session","source":"string","destination":"string","target":"Location","targetRevision":"u64","consentId":"UUID?","expires":"Instant","reservationId":"UUID","state":"REQUESTED|RESERVED|SWITCHING|JOINED|ARRIVAL_AUTHORIZED|ARRIVED|COMMITTED|FAILED|CANCELLED|UNCERTAIN"},"ArrivalReceipt":{"operationId":"UUID","destinationSession":"Session","location":"Location","worldTick":"u64","outcome":"ARRIVED|REFUSED|UNCERTAIN","durableSequence":"u64"}},"methods":["reserve(intent:TransferIntent) -> Result<TransferIntent>","authorizeArrival(operationId:UUID, joinedSession:Session) -> Result<TransferIntent>","finalizeArrival(receipt:ArrivalReceipt) -> Result<TransferIntent>","reconcile(operationId:UUID) -> Result<TransferIntent>"],"errors":["SESSION_CHANGED","CONSENT_EXPIRED","DESTINATION_UNREADY","SAFETY_CHANGED","TRANSFER_FAILED","ARRIVAL_UNCERTAIN"],"ownership":"Proxy coordinates connection; destination owns final safety and durable receipt; connection callback alone is never success."},
    {"id":"SEF-IF-009","name":"Audit journal and coverage","producer":"SEF-PHASE-008","consumers":["SEF-PHASE-009","SEF-PHASE-010","SEF-PHASE-011","SEF-PHASE-012","SEF-PHASE-013","SEF-PHASE-014","SEF-PHASE-015"],"requirements":["SEF-REQ-024","SEF-REQ-026","SEF-REQ-031"],"acceptance":["SEF-AC-024","SEF-AC-026","SEF-AC-031"],"records":{"EventKey":{"originId":"string","journalEpoch":"UUID","sequence":"u64","eventId":"UUID"},"AuditEvent":{"key":"EventKey","world":"WorldRef?","session":"Session?","kind":"enum_string","outcome":"ATTEMPTED|DENIED|CANCELLED|APPLIED|PARTIAL|FAILED|UNKNOWN","actor":"Actor","initiator":"Actor?","confidence":"DIRECT|PROPAGATED|INFERRED|UNKNOWN","causeId":"UUID?","parentEvent":"EventKey?","utc":"Instant","tick":"u64?","before":"Snapshot?","after":"Snapshot?","sensitivity":"set<PUBLIC_METADATA|PRIVATE_CONTENT|HIDDEN_ACTIVITY|RESTORATION_PAYLOAD>","fieldPolicy":"map<field_path,set<permission_class>>","reversibility":"EXACT|CONDITIONAL|NONE","adapterId":"string","adapterVersion":"u32","redactions":"Redaction[]"},"Watermarks":{"captured":"u64","localDurable":"u64","centralDurable":"u64","queryVisible":"u64","gaps":"Gap[]","uncertainTail":"bool"},"Gap":{"first":"u64?","last":"u64?","reason":"enum_string","categories":"string[]","acknowledged":"bool?","utcStart":"Instant?","utcEnd":"Instant?"}},"methods":["append(event:AuditEvent) -> Result<LocalDurabilityReceipt>","ingest(batch:AuditEvent[]) -> Result<CentralCommitReceipt>","registerCoverage(adapter:CoverageDescriptor) -> Result<CoverageReceipt>","health(actor:Actor) -> Result<AuditHealth>"],"errors":["JOURNAL_UNAVAILABLE","CAPACITY_LOSS","OVERSIZED_PAYLOAD","REDACTED_NONREVERSIBLE","SQL_UNAVAILABLE","COVERAGE_GAP"],"ownership":"Origin owns sequence and local journal; central transaction deduplicates event key and watermark; no debug sampling applies."},
    {"id":"SEF-IF-010","name":"Audit query and extension API","producer":"SEF-PHASE-010","consumers":["SEF-PHASE-011","SEF-PHASE-015"],"requirements":["SEF-REQ-025","SEF-REQ-029"],"acceptance":["SEF-AC-025","SEF-AC-029"],"records":{"AuditQuery":{"queryId":"UUID","actor":"Actor","scope":"AuthorizedScope","filters":"TypedFilter[]","cutoffs":"map<origin_epoch,u64>","pageSize":"u16","cursor":"opaque128?","includeSensitive":"bool"},"QueryPage":{"queryId":"UUID","events":"AuditEventView[]","nextCursor":"opaque128?","counts":"TypedCounts","watermarks":"map<origin_epoch,Watermarks>","coverage":"CoverageDescriptor[]"}},"methods":["lookup(query:AuditQuery) -> Result<QueryPage>","export(query:AuditQuery, format:JSONL|CSV) -> Result<ExportReceipt>","lookupPending(actor:Actor, world:WorldRef) -> Result<PendingView>","registerAdapter(descriptor:CoverageDescriptor, adapter:AuditAdapterV1) -> Result<CoverageReceipt>"],"errors":["SCOPE_DENIED","SENSITIVE_DENIED","QUERY_LIMIT","CURSOR_EXPIRED","HISTORY_INCOMPLETE","ADAPTER_UNSUPPORTED"],"ownership":"Async bounded SQL service; every page, detail, hover and export reauthorizes; API rights separate append/query/restore."},
    {"id":"SEF-IF-011","name":"Restoration job","producer":"SEF-PHASE-011","consumers":["SEF-PHASE-015"],"requirements":["SEF-REQ-028","SEF-REQ-029"],"acceptance":["SEF-AC-028","SEF-AC-029"],"records":{"RestoreJob":{"jobId":"UUID","actor":"Actor","mode":"ROLLBACK|RESTORE|UNDO","parentJob":"UUID?","selectedEvents":"EventKey[]","selectionDigest":"sha256","cutoffs":"map<origin_epoch,u64>","resources":"ResourceRef[]","expectedCurrent":"map<ResourceRef,sha256>","expires":"Instant","conflictPolicy":"STOP","state":"PREVIEW|CONFIRMED|RUNNING|PARTIAL|COMPLETED|CANCELLED|UNCERTAIN","applied":"u64","skipped":"u64","failed":"u64","unknown":"u64"},"RestoreStep":{"stepId":"UUID","jobId":"UUID","dependencies":"UUID[]","beforeDigest":"sha256","afterDigest":"sha256","state":"INTENT|APPLIED|CONFLICT|FAILED|UNCERTAIN"}},"methods":["preview(query:AuditQuery, mode:RestoreMode) -> Result<RestoreJob>","apply(actor:Actor, jobId:UUID, selectionDigest:sha256, confirmation:opaque128) -> Result<RestoreJob>","cancel(actor:Actor, jobId:UUID) -> Result<RestoreJob>","undo(actor:Actor, completedJob:UUID) -> Result<RestoreJob>"],"errors":["PREIMAGE_MISSING","LIVE_CONFLICT","CONSERVATION_UNPROVEN","REGISTRY_MISSING","WORLD_REPLACED","JOB_UNCERTAIN","OFFLINE_RESOURCE"],"ownership":"Durable job selection and step ledger; owning backend executes bounded causal groups; SQL and world are not one transaction."},
    {"id":"SEF-IF-012","name":"RTP allocation","producer":"SEF-PHASE-012","consumers":["SEF-PHASE-013","SEF-PHASE-014","SEF-PHASE-015"],"requirements":["SEF-REQ-032"],"acceptance":["SEF-AC-032"],"records":{"GridIdentity":{"gridId":"UUID","generation":"u64","world":"WorldRef","minX":"i32","maxX":"i32","minZ":"i32","maxZ":"i32","parentRows":"u16","parentColumns":"u16","childRows":"u16","childColumns":"u16","policyRevision":"u64"},"Allocation":{"reservationId":"UUID","operationId":"UUID","session":"Session","grid":"GridIdentity","parentCycle":"u64","parentId":"u32","childCycle":"u64","childId":"u32","candidate":"Location?","state":"RESERVED|PREPARED|ARRIVAL_AUTHORIZED|ARRIVAL_OBSERVED|COMMITTED|RELEASED|QUARANTINED","expires":"Instant","fencingToken":"u64"}},"methods":["reserve(session:Session, gridId:UUID, operationId:UUID) -> Result<Allocation>","prepare(reservationId:UUID, candidate:Location) -> Result<Allocation>","commit(reservationId:UUID, receipt:ArrivalReceipt) -> Result<Allocation>","release(reservationId:UUID, proof:NonArrivalProof) -> Result<Allocation>","reconcile(reservationId:UUID) -> Result<Allocation>"],"errors":["INVALID_GRID","CYCLE_BLOCKED","NO_UNUSED_CELL","PERSISTENCE_UNAVAILABLE","FENCED","ARRIVAL_UNCERTAIN"],"ownership":"Backend serialized durable SQLite transaction; only committed successful landings consume parent and child turns."},
    {"id":"SEF-IF-013","name":"RTP safety and prepared candidates","producer":"SEF-PHASE-013","consumers":["SEF-PHASE-014","SEF-PHASE-015"],"requirements":["SEF-REQ-033"],"acceptance":["SEF-AC-033"],"records":{"SafetyPolicy":{"revision":"u64","minimumDistanceXZ":"finite f64","recentWindowSeconds":"u32","allowedBiomes":"resource_location[]?","allowChunkGeneration":"bool"},"PreparedCandidate":{"id":"UUID","gridGeneration":"u64","parentId":"u32","childId":"u32","location":"Location","policyRevision":"u64","observedTick":"u64","expires":"Instant","chunkRevision":"u64"}},"methods":["findCandidate(allocation:Allocation, policy:SafetyPolicy) -> Result<PreparedCandidate>","validateAndLand(allocation:Allocation, candidate:PreparedCandidate, session:Session) -> Result<ArrivalReceipt>"],"errors":["TOO_CLOSE","UNSAFE","CLAIM_DENIED","CHUNK_UNREADY","CANDIDATE_EXPIRED","QUEUE_TIMEOUT","RECENT_LEDGER_FULL"],"ownership":"Immutable prepared proposals spend no turns; final safety and actual teleport occur on backend thread and recheck all players including vanished players."},
    {"id":"SEF-IF-014","name":"RTP routing telemetry and leases","producer":"SEF-PHASE-014","consumers":["SEF-PHASE-015"],"requirements":["SEF-REQ-034","SEF-REQ-035"],"acceptance":["SEF-AC-034","SEF-AC-035"],"records":{"LoadSample":{"backendId":"string","backendBoot":"UUID","sequence":"u64","configGeneration":"u64","sampleDurationMillis":"u32","meanMspt":"finite f64","p95Mspt":"finite f64","spikeFraction":"finite f64","players":"u32","playerCapacity":"u32","incoming":"u32","chunkWork":"u32","chunkCapacity":"u32","eligiblePrepared":"u32","preparedTarget":"u32","ready":"bool","profileDigest":"sha256"},"AdmissionLease":{"leaseId":"UUID","operationId":"UUID","session":"Session","backendId":"string","backendBoot":"UUID","configGeneration":"u64","expires":"Instant","reservationId":"UUID?","state":"HELD|ARRIVAL_AUTHORIZED|COMMITTED|RELEASED|UNCERTAIN"}},"methods":["choose(session:Session, eligibleWorlds:string[]) -> Result<AdmissionLease>","renew(lease:AdmissionLease) -> Result<AdmissionLease>","finish(leaseId:UUID, receipt:ArrivalReceipt) -> Result<AdmissionLease>"],"errors":["TELEMETRY_STALE","CAPACITY_FULL","PROFILE_INCOMPATIBLE","NO_ELIGIBLE_BACKEND","LEASE_FENCED","ARRIVAL_UNCERTAIN"],"ownership":"Proxy selects backend first and serializes admission; destination owns allocation and final landing."},
    {"id":"SEF-IF-015","name":"Lifecycle templates and durable visits","producer":"SEF-PHASE-006","consumers":["SEF-PHASE-007","SEF-PHASE-009","SEF-PHASE-014","SEF-PHASE-015","SEF-PHASE-016"],"requirements":["SEF-REQ-036"],"acceptance":["SEF-AC-036"],"records":{"LifecycleConfig":{"schema":"u16=1","generation":"u64","scope":"AUTO|LOCAL|NETWORK","allowPlayerOverrides":"bool","families":"map<JOIN|LEAVE|WELCOME|WELCOME_BACK,TemplateFamily>"},"TemplateFamily":{"enabled":"bool","audience":"VISIBLE_PUBLIC|SUBJECT|AUTHORIZED_STAFF","selection":"RANDOM_NO_IMMEDIATE_REPEAT|ROUND_ROBIN","variants":"TemplateVariant[1..32]"},"TemplateVariant":{"id":"bounded_identifier","lines":"TemplateLine[1..8]","hoverLines":"TemplateLine[0..4]"},"TemplateLine":{"runs":"TemplateRun[1..16]"},"TemplateRun":{"template":"bounded_template_string","role":"PRIMARY|LABEL|VALUE|SUCCESS|WARNING|ERROR|LOCATION|EDIT|METADATA","bold":"bool"},"Visit":{"scopeId":"string","playerId":"UUID","firstAdmittedAt":"Instant?","lastAdmittedAt":"Instant?","lastCompletedAt":"Instant?","activeVisitId":"UUID?","activeSession":"Session?","completedVisits":"u64","baseline":"KNOWN_RETURNING|RECORDED_FIRST","revision":"u64"},"LifecycleEvent":{"eventId":"UUID","visitId":"UUID","scopeId":"string","session":"Session","family":"JOIN|LEAVE|WELCOME|WELCOME_BACK","readyBackend":"string","world":"WorldRef?","previousAdmittedAt":"Instant?","previousCompletedAt":"Instant?","configGeneration":"u64","visibilityRevision":"u64","cause":"ADMISSION_READY|DISCONNECT_CONFIRMED","firstRecordedVisit":"bool"},"DeliveryClaim":{"claimId":"UUID","eventId":"UUID","family":"JOIN|LEAVE|WELCOME|WELCOME_BACK","variantId":"string","templateGeneration":"u64","recipientId":"UUID","recipientSession":"Session","state":"CLAIMED|SUBMITTED|SUPPRESSED|EXPIRED|UNCERTAIN","reason":"enum_string","expires":"Instant"},"VisitDecision":{"visit":"Visit","classification":"FIRST_RECORDED|RETURNING","previousAdmittedAt":"Instant?","previousCompletedAt":"Instant?","events":"LifecycleEvent[]","duplicate":"bool","durableSequence":"u64"},"CompiledLifecycleSnapshot":{"generation":"u64","effectiveScope":"LOCAL|NETWORK","authorityId":"string","configDigest":"sha256","templates":"map<family_variant_id,BoundedLiteralTemplateAst>","enabledFamilies":"set<JOIN|LEAVE|WELCOME|WELCOME_BACK>","compiledAt":"Instant"}},"methods":["compileLifecycle(config:LifecycleConfig) -> Result<CompiledLifecycleSnapshot>","admitVisit(session:Session, readyBackend:string, world:WorldRef?, admissionId:UUID) -> Result<VisitDecision>","closeVisit(visitId:UUID, session:Session, endedAt:Instant, reason:enum_string) -> Result<VisitDecision>","claimDelivery(event:LifecycleEvent, recipient:Actor, recipientSession:Session) -> Result<DeliveryClaim>","preview(actor:Actor, family:LifecycleFamily, variantId:string, subject:UUID?) -> Result<Message[]>"],"errors":["INVALID_TEMPLATE","UNKNOWN_PLACEHOLDER","TEMPLATE_LIMIT","LIFECYCLE_AUTHORITY_UNAVAILABLE","STALE_SESSION","DUPLICATE_LIFECYCLE_EVENT","VISIBILITY_DENIED","DELIVERY_UNCERTAIN","PREVIEW_DENIED","MIGRATION_CONFLICT"],"ownership":"Standalone backend owns LOCAL visits and delivery. NETWORK proxy owns one durable visit/claim ledger and emits once after backend readiness; backend suppresses duplicate lifecycle announcements. Commit claims before dispatch, never replay ambiguous client display."}
  ]
}
```

Sensitivity labels are composable. An event or field carrying multiple labels requires the intersection of all corresponding permissions for metadata, body, linked records, hover, export and restoration reads; one granted capability never reveals another restricted class. Safe summaries omit unauthorized fields and identities without implying the event was absent.

When `Session.proxyBoot` is absent, a standalone backend issues a locally persisted player connection epoch in its own authority namespace, bound to backend boot and player UUID. The same backend issues a durable local ArrivalReceipt after final safety and actual movement. Local home/RTP operations use these receipts and require no proxy process, network admission or fabricated proxy epoch.

Supporting response types in the registry have these fixed semantics. `CommitReceipt` includes operation ID, committed revision, durable sequence and duplicate flag. `StateSnapshot` includes authority boot, final revision, ordered rows and checksum. `OperationResult` includes operation ID, explicit state, applied count and safe reason. `AudienceContext` includes actor, optional session, locale, platform and authorized scope. `CaptureStatus` includes side, target, categories, remaining seconds/events/bytes and exact output path. `AuthorizedScope` contains allowed backend/world IDs and data classes; it cannot be widened by filters. `Snapshot` contains bounded codec bytes, codec/adapter version, registry digest, exactness and redaction markers. `CoverageDescriptor` contains platform/mod versions, family, hook, adapter version and supported/partial/unsupported/disabled/redacted/unhealthy state. `ResourceRef` uses world generation plus block/entity/inventory/slot identity. `NonArrivalProof` contains fenced session/operation identity and a durable terminal refusal or independently reconciled non-arrival. `ExportReceipt` includes canonical owned path, digest, row count, cutoff and omissions. No omitted subtype permits unbounded arbitrary data.

### Security and authority

The bridge uses TLS 1.3 mutual authentication on an existing private binding, with registered peer identities and distinct administrative keys from forwarding secrets. Provisioning is an operator setup contract, not permission to collect credentials. Per-peer certificate rotation permits at most two explicitly registered keys for a bounded overlap, then revokes the old key and fences old channels. Validate length before allocation, protocol/type before decode, peer/boot/channel epoch and monotonic sequence before dispatch, and actor/session/revision before authorization. Defaults are 256 KiB per frame, 128 queued messages per peer, 64 in-flight requests, a five second request deadline and a maximum 30 second bridge-envelope authorization lifetime. Snapshot chunks are bounded and checksummed; incomplete snapshots never replace state. Malformed or replayed messages cannot invoke a command string. Legacy administrative plugin channels are consumed and rejected rather than forwarded to clients.

Network state changes commit to the single SQLite writer before publication. Backends accept contiguous revisions, ignore known duplicates and request a fresh snapshot on gaps. Snapshot installation is atomic. Absolute UTC mute expiration is persisted; an already active mute remains enforced during outage until its known expiry, while new network mutations and stale login admission are refused. A backend lacking an initial authoritative moderation snapshot is not network ready. Existing local gameplay can continue under last validated state; it does not manufacture global success. Ban changes report committed and applied/disconnection status separately. Visibility is computed for the observer on every list, resolution, selector, suggestion, tab and entity distribution path; preexisting entities are reconciled on transitions. Authorization rechecks at both proxy and world owner include target hierarchy, consent and source console scope.

Qualified homes use owner/backend/normalized-name uniqueness and stable IDs. Normalize names with Unicode NFC and locale-independent case folding, preserve display text, enforce a bounded length, and reject control characters. A plain name chooses an allowed current-server home first, otherwise the sole allowed remote match. Multiple remote matches return explicit permitted choices; never choose arbitrary order. `home survival cabin` is a concrete example of the proposed deterministic cross-server form: the first argument is a configured backend ID and the second is that owner's normalized home name. Rename/delete actions bind stable ID and revision. World replacement changes worldGeneration and invalidates old arrival eligibility rather than reusing a dimension name.

#### Supported network profiles

Backend profiles identify exact server-owned platform, installed mods, registries, relevant configuration and adapters. Client profile classification uses only negotiated handshake, mod-channel and registry facts actually observable by the pinned proxy/Forge adapters, bound to an operator-tested directed transfer relation. `CompatibilityRelation.clientProfileDigest` is the digest of this normalized observed classification and its evidence schema, not an attestation of complete client files or configuration. Client-only mods and settings outside that handshake are unobservable and must be documented as such. Exact server and adapter digests remain authoritative. An unknown, stale or unbindable session refuses cross-profile travel; neither a display pack name nor a guessed complete client inventory can authorize it. No new SEF client handshake or required client component is introduced. Real client tests prove the particular directed relation for the observed profile, not arbitrary unseen client modifications.

#### Failure and recovery contract

Failures distinguish invalid input, denied authority, unavailable dependency, conflict, confirmed non-arrival, partial application and uncertain world outcome. Retryable failures retain operation IDs; destructive or uncertain work never retries blindly.

Transfers reserve the destination before switching. TPA consent binds requester, recipient, both session generations, destination and expiry; switches, disconnect, permission changes and warmup movement invalidate or explicitly revalidate it. The destination proves the intended player joined, rechecks safety and records arrival. Back history records actual successful departure/arrival with server-qualified locations, never attempted transfers. Timeout after arrival authorization is uncertain, not proof of failure. Recovery compares durable receipts and actual player/session state before releasing a reservation or retrying. A denied arrival never becomes success because the proxy connected the player; safe failure messaging distinguishes connection and movement, preserves source state before switch and avoids unsolicited automatic fallback travel.

### State schemas and recovery

The canonical coverage inventory is `docs/features/audit/coverage.md`; functional parity is `docs/features/audit/parity.md`. Each action row records source version, Forge applicability, actual mutation hook, cause confidence, exact snapshot boundary, redaction, reversibility, adapter version, required fixture and evidence. Required families include blocks and block entities; attached/multiblock changes; falling blocks/pistons; fire/explosions; liquids/buckets; growth/decay/crops/trees; portals/snow/sculk; version-applicable eggs/archaeology/pots; containers/player inventories/cursor/equipment; simulation versus real capability transfer; hoppers/pipes/dropper/dispenser; crafting/trading/smelting/remainders; item creation/merge/split/use/damage/destruction; entity spawn/damage/death/removal/passengers/drops; accepted movement and rotation, vehicle/portal/respawn transitions; sign faces and text; commands and full communications; identity/session/network lifecycle; moderation/vanish/permissions/configuration; investigation and restoration themselves.

Record attempts separately from applied changes. Verify post-mutation boundaries, including two opposing changes within one tick. Nearest-player attribution is forbidden. Scoped cause contexts propagate across scheduled work explicitly and clear after use. Fake players retain their actual identity and origin rather than silently becoming a machine owner. Movement captures accepted authoritative changes, not rejected packets or a sampled tick trajectory; unchanged internal ticks are not events. Supported modpacks receive explicit adapter inventories. An unavailable hook produces visible coverage gaps and cannot close mandatory feasible behavior. Newer vanilla content absent from 1.20.1 is marked version-inapplicable. Bukkit/Folia APIs and unavailable patron internals are mapped as platform-incompatible or unknown, not claimed implemented. Forge WorldEdit integration is required when present in the supported pack inventory; arbitrary absent mods are not added.

Journals assign `(originId, journalEpoch, sequence)` before enqueue, retain immutable IDs across retries, frame records with length/schema/checksum and expose captured, local durable, central durable and query-visible watermarks separately. Default group fsync is at most 100 ms or 1 MiB, whichever first; this is an explicit crash uncertainty window, not a zero-loss guarantee. SQL commits rows and watermark in one InnoDB transaction before acknowledging. Distinct world generations prevent same-coordinate collisions. Torn or corrupt tails are quarantined, valid prefixes replay idempotently and uncertain intervals remain visible. Generic regexes cannot prove detection of unknown secrets: command schemas and typed NBT adapters define sensitive paths before the first sink. If exact restoration data cannot be retained safely, mark nonreversible.

Initial bounded operating defaults are 64 MiB capture memory, 64 MiB segments, 4 GiB local spool, 16 MiB reserved loss ledger, 512 events or 1 MiB per ingest batch, four ingest connections and two query connections per process. Payloads above 1 MiB use bounded chunked blobs up to 8 MiB; larger or over-depth state records explicit omission and cannot claim exact reversal. Capacity warnings start at 75 percent and critical at 90 percent. These are conservative initial limits, not measured capacity promises. Phase 015 publishes measured event/byte rates and validates tuning. Mandatory capture is never sampled to meet a budget. Saturation or exhausted storage under DEC-012 reports exact known loss or unknown extent, rather than quietly dropping.

A local segment fully acknowledged by committed central storage and beyond the configured local recovery window can be removed as redundant transport cleanup. Record its local rotation count without creating a central-history gap. Emergency deletion of an unacknowledged segment creates an actual loss range; an acknowledged segment lost locally while its central copy is independently unverified produces uncertainty until checked. Always distinguish these states in alerts and queries. Normal retention defaults to manual scoped purge. Emergency rotation selects only the oldest verified closed SEF-owned segment in the canonical spool directory, writes/coalesces loss metadata first when possible, checks ownership and symlink boundaries, and never touches active segments, authority databases, jobs, RTP state or central SQL rows. If the loss ledger also fails, expose in-memory degraded counters and an unknown restart interval. Queries and restoration eligibility carry loss/purge/unreplicated boundaries. Continuing gameplay does not imply successful administration when its authority database is full.

Normal retention also supports explicitly enabled scheduled purge under DEC-010. Phase 008 owns the validated policy and safe manual default; Phase 010 owns both manual preview/confirm execution and the disabled-by-default scheduler. An operator must preview and confirm the exact scheduled policy revision, including dataset, world scope, minimum age and UTC schedule, before it can delete anything. Each scheduled run freezes its cutoff and selected EventKeys, uses the same dependency guards and durable intentional-purge boundaries as manual execution, and serializes against other purge jobs and migration through a fenced dataset lease. Unknown or unavailable restoration dependencies block deletion. Policy changes invalidate authorization; invalid reload preserves the previous valid snapshot. Disabling scheduling cancels future batches. No missed-run catch-up, automatic table optimization, capacity-triggered SQL purge, or deletion from authority and job stores is permitted. The scheduler works on a standalone Forge backend without Velocity and does not need a proxy to coordinate a shared audit dataset. Phase 010 defines the configuration, bounded execution and race-recovery fixtures; Phases 011, 015 and 016 prove real job protection and operator procedures.

Investigation uses stable per-origin snapshot cutoffs, bounded typed filters, default ten and maximum 50 rows per chat page, 15 second query deadlines, ten minute cursor expiry and explicit continuation. Filters cover actor/pseudo-actor, origin, world, radius/box/selection/global scope, UTC/relative time, event/action/outcome, positive and negative registry filters, literal text-prefix inclusion and exclusion, confidence and correlation. Counts and material/actor summaries use the same authorization. Pending local events are distinguishable from durable SQL results. Every page, hover, linked record, location jump and export rechecks visibility and permissions. Exports are explicit, sanitized, maximum 100,000 rows or 64 MiB per job, confined to an operator-configured owned directory and include completeness metadata.

Restoration selects immutable event IDs and snapshot cutoffs, expires previews after 60 seconds, hashes the selection and resource preconditions, and requires explicit confirmation. Default radius is ten blocks and maximum 100 without a separately authorized broader scope. Default job cap is 10,000 selected events; larger requests must be explicitly partitioned with disclosed independence, not silently truncated. Rollback follows reverse causal dependency groups; restore and undo refer to the exact applied job, never a fresh broad query. Sorted resource locks, bounded owner-thread batches and before/after fingerprints protect newer edits. Defaults are at most 64 mutations or two ms per server tick, four chunk tickets and five seconds to acquire resources. Stop on conflict by default, preserve partial counts and release tickets/locks on every terminal path.

Transfers of items reverse both endpoints and associated drops as one conservative group. A full destination, unavailable offline inventory, onward movement/crafting without a provable dependency chain, duplicate entity UUID or missing registry prevents exact reversal. Do not spill items, invent compensation or resurrect death drops twice. Durable step intent precedes world mutation and durable applied acknowledgment follows it. On crash, matching before permits a safe retry, matching after permits reconciliation, anything else quarantines. SQL rollback does not undo Minecraft state. Multi-backend jobs expose partial progress rather than claiming distributed atomicity. Recorded-item give is a distinct item-creation action with permission `sef.audit.give`, disabled by default even for audit wildcard, explicit confirmation and separate audit origin.

Preview uses private vanilla block-change packets where the pinned platform can represent the selected changes, plus complete text summaries and conflict reports for all selected resources. No server mutation occurs during preview. Cancellation, expiry, disconnect, chunk reload and resource changes restore authoritative display. Read permission never grants preview apply, purge, sensitive reads, export, hidden activity or give. The extension API exposes original typed append/query/coverage/guarded-job methods with distinct rights and tested Forge adapters; it does not reproduce Bukkit binary types. Functional parity is closed per applicable feature and API operation, not by broad category alone. Consumer pause suspends ingestion while preserving local capture and visible backlog; separately authorized capture pause records its missing-history interval. A backlog alone is not evidence loss.

Discord is an optional configured delivery destination for operational incidents only. Restricted `sef audit health` and `sef audit incidents` remain authoritative local visibility for every authorized operator/admin. Notify on state transitions, coalesce by incident, bound outbox to 128 entries, retry at most eight times per episode and honor server rate-limit delays. No private text, NBT, full commands, credentials, webhook URL or mass mentions are sent. HTTPS destination configuration is operator-only, redirects are disabled, allowed Discord webhook origins are validated and arbitrary player URLs are rejected. Delivery failure remains visible locally. Acceptance uses an owned mock endpoint, never a production message.

### Product behavior and command ownership

The proposed shared administrative root is `/sef`; existing retained aliases remain catalog-controlled after collision inspection. Console equivalents omit `/` and never require a player for status, diagnostics or explicitly console-safe administration. Proposed new families are `sef audit`, `sef action`, `sef debug` and `sef rtp`; `/rtp` is the player entry alias. Commands are proposals until registered and verified. No reference command is assumed implemented merely because its name appears here.

Use semantic teal `#00fb9a` for success/primary action, gray labels, bold useful names and values, red `#ff3300` and soft red `#ff7e5e` for errors, gold `#ffc43b` for location/toggle, violet `#c160ff` for edits and blue `#3b76ff` for server/world metadata. Original localized templates group labeled details and bracket actions such as `[✓ Accept]` and `[✕ Decline]`, with descriptive hover and previous/next pagination. Glyphs always have words and an ASCII/accessibility option. Components must wrap sensibly, preserve readable contrast and offer keyboard-enterable equivalents and plain console text. Administrative feedback is a system message, never forged signed player chat.

Names, reasons, descriptions, log text and registry strings are literal component children. Do not parse user markup or concatenate raw values into executable links. `sef action <opaque-token>` is the only generated executable action route; safe suggestions prefill editable commands but still validate normal execution. Tokens are recipient/session/revision/snapshot bound, expire after 60 seconds, use 128-bit randomness and have at most 64 live entries per recipient. Redemption is atomic/idempotent and rechecks current authority. Destructive delete/purge/apply/give actions issue a separate confirmation showing scope and consequences. Scheduled purge confirms the policy revision at activation, then rechecks that authorization and scope before each bounded run; it never treats an unconfirmed config edit as consent. Hover and labels are filtered by the same visibility and sensitive-data policy as the action. `docs/features/commands/presentation-coverage.md` enumerates every registered command and success, denial, validation, empty, partial, unavailable, stale and confirmation result.

### Configurable lifecycle messages

SEF-REQ-036 is implemented canonically in Phase 006 after the configuration, renderer, network authority and observer-visibility contracts exist. Phase 003 preserves implemented social commands and inventories their template/reminder coupling; it does not implement a competing network lifecycle service. Phase 006 replaces the corresponding connection-message dispatch through this contract, and Phase 015 closes complete local/network rendering and crash/reconnect acceptance. Join, leave, first welcome and welcome-back are four independently enabled message families edited in the existing canonical main configuration under `messages.lifecycle`. This is a text configuration editor, not a screen or in-game menu. Preserve each product's verified main filename and serialization format; `sef messages status` reports its exact resolved path, effective authority, generation and any ignored local settings.

#### Schema, defaults and template grammar

The version 1 schema is SEF-IF-015. Main settings are `schema=1`, `scope=AUTO`, `allow_player_overrides=false`, and four keyed families `join`, `leave`, `welcome`, `welcome_back`. Every family has `enabled`, `audience`, `selection` and `variants`. All four are enabled by default. Join/leave default to `VISIBLE_PUBLIC`; first/returning welcome default to `SUBJECT`. Supported alternative audiences are `SUBJECT` and `AUTHORIZED_STAFF`; the latter requires `sef.messages.staff` and still cannot bypass `sef.vanish.see`. Audience configuration never disables observer visibility or grants sensitive-placeholder access.

Default selection is `RANDOM_NO_IMMEDIATE_REPEAT`. Choose uniformly among stable variant IDs other than the last selected ID for the same subject UUID, family and authority scope. A single configured variant necessarily repeats and is explicitly allowed. The alternate policy `ROUND_ROBIN` follows the configured variant order and persists the next ID; duplicate callbacks do not advance either policy. Persist the chosen variant and predecessor in the same authority transaction as the lifecycle event, before recipient dispatch. The same event uses the same variant for all authorized recipients, with recipient-specific visible values. Reordering an unchanged random-policy list does not reset history. On reload, preserve the previous variant by ID when still present; a removed ID permits selection from the remaining valid set. Round-robin resumes from the surviving next ID, otherwise starts at the first configured ID. Changing policy does not replay the current event.

Each family requires one to 32 variants, each with a unique ASCII ID of one to 48 characters from letters, digits, underscore and period. A variant contains one to eight message lines and zero to four hover lines. A line contains one to 16 typed runs, each with `template`, semantic `role` and `bold`; defaults are `PRIMARY` and false. Total template text per variant, including hover, is at most 4096 Unicode code points; any run is at most 1024. The complete lifecycle subsection is at most 256 KiB UTF-8. Rendered message plus hover is at most 16,384 code points and 64 KiB serialized components. Validation rejects excessive configuration; runtime expansion overflow suppresses that event with a bounded diagnostic instead of sending truncated misleading text.

Templates use the shared renderer's typed runs, not a new markup engine. Allowlisted semantic roles use the existing teal/gray/value/error palette. Standard player/value runs are bold and contextual labels gray. For example, a join variant named `greeting` has a primary run `✓ `, a bold value run `{player_name}`, and a label run ` joined the server.`. The second default join variant says `✓ Welcome, {player_name}.`; leave defaults are `{player_name} left the server.` and `Goodbye, {player_name}.`; first welcome defaults are `✓ Welcome, {player_name}!` and `✓ Glad you are here, {player_name}.`; returning welcome defaults are `✓ Welcome back, {player_name}!` and `✓ Good to see you again, {player_name}.`. Each placeholder is a separate bold value run in generated defaults. Provide the same original localized default keys, plain console rendering and glyph alternatives as SEF-IF-004. Operator-authored text is preserved as written and is not silently translated.

A single brace pair denotes exactly one allowlisted placeholder. `{{` and `}}` produce literal braces. Unmatched braces, nested expressions and unknown placeholder keys are invalid. Literal run text never parses MiniMessage, legacy formatting codes, URLs, command links, expressions or external placeholder providers. A configured run cannot specify arbitrary click actions; any product-provided action remains a typed registered SEF action under SEF-IF-004. Line breaks are represented by the bounded line list, not embedded newline/control characters inside a run. Replacement values are literal children and are never reparsed, even if a nickname contains braces, brackets, tags or command separators. Strip control and bidi override characters from replacement display values, bound them to 256 code points and indicate elision with a visible ellipsis. Do not mutate the stored username, nickname or identity.

| Placeholder | Typed value and semantics | Availability and privacy rule |
|---|---|---|
| `{player_name}` | Authenticated canonical current username, literal text | Always available after admission; leave uses the admitted identity snapshot |
| `{player_display_name}` | Current authorized nickname/display text with interactive metadata removed | Fall back to canonical username; no foreign click/hover or hidden identity metadata |
| `{player_uuid}` | Canonical UUID string | Available to the subject or an observer permitted to inspect that identity; otherwise `Not available` |
| `{server_name}`, `{server_id}` | Configured display label and stable backend ID for the admitted backend, or last ready backend on leave | Never use a hostname or private address; if server discovery is denied, use `Not available` |
| `{world_name}`, `{dimension}` | Configured world display label and dimension resource key from a ready backend snapshot | If absent, stale or location-discovery policy denies access, use `Not available`; never coordinates |
| `{online_count}` | Integer count of players visible to this recipient in the effective LOCAL or NETWORK scope after the transition | Joining subject included when visible, departing subject excluded; vanished players do not inflate unauthorized counts |
| `{max_players}` | Operator-configured advertised capacity for that scope | Use configured public capacity, never infer hidden backend population or hardware |
| `{first_visit}` | Localized Yes/No for first recorded successful admission in this authority scope | Subject or `sef.messages.history` only; public templates receive `Not available` otherwise |
| `{previous_login_at}` | UTC ISO-8601 instant of the prior committed successful admission, captured before updating this visit | Subject or `sef.messages.history` only; missing/imported-unknown value is `Not available` |
| `{previous_visit_at}` | UTC ISO-8601 close time of the most recent earlier confirmed completed visit | Same history authorization; never the current admission timestamp or a guessed crash-disconnect time |
| `{previous_visit_ago}` | Localized elapsed duration since that prior completed visit, rounded to minutes | Same authorization; clamp a backward clock difference to zero and record clock anomaly; missing value is `Not available` |

The placeholder catalog is closed in version 1. Credentials, IP addresses, precise coordinates, unrestricted permission data and hidden presence are not configurable expansions. Unavailable strings are localized literals and never the text `null`, an empty fabricated timestamp or an evaluation expression. Both body and hover enforce the same per-field permission intersections. Compile-time rejection applies to unknown keys even in a currently disabled family, preventing a later enable from activating unvalidated input.

#### Authority, admission and visit semantics

`AUTO` resolves once at startup to LOCAL on a standalone backend and NETWORK when the authenticated SEF network mode is configured. It does not switch to LOCAL during a bridge outage. Explicit LOCAL is valid only on a standalone backend with SEF network mode disabled; it owns its local visit ledger and announcements. A LOCAL request while SEF network mode is enabled is a configuration conflict and cannot enable duplicate backend broadcasts. Explicit NETWORK requires configured network authority or returns an invalid configuration. In network mode, the proxy's main configuration owns the NETWORK templates and the proxy alone schedules network announcements. Backend main-config lifecycle settings remain preserved for standalone operation and are reported inactive. Backends suppress their SEF and vanilla join/leave announcements in that mode, including when a family is disabled, so disabling cannot expose a vanilla vanish leak. Local mode likewise replaces the corresponding vanilla lifecycle dispatch. Compatibility checks identify any third-party announcer that duplicates these paths; no unsupported claim is made about an unrelated mod's independent broadcast.

No backend-switch announcement is enabled or implied by these four families. A successful backend transfer within the same authenticated network connection preserves the visit ID and first/returning classification, emits neither network join/leave nor another welcome, and updates only the last ready backend/world snapshot. Transfer retries and destination connection callbacks cannot create visits. A true disconnect followed by a new authenticated connection creates a new visit and receives one returning welcome; repeated callbacks or bridge reconnect for the same admission ID do not. Do not debounce distinct genuine visits merely because their timestamps are close.

LOCAL successful admission requires the backend's player login lifecycle to reach actual world-ready state after moderation and visibility initialization. NETWORK admission requires proxy authentication plus an authenticated backend-ready receipt for the matching UUID, connection epoch, backend boot and current presence. This Phase 006 receipt is readiness, not the Phase 007 teleport-arrival protocol; it uses existing Phase 005 presence and bridge messages and creates no dependency on future travel implementation. Ban rejection, failed handshake, incompatible profile, failed backend connection or disconnect before readiness creates no visit or announcement.

Visit identity is durable UUID plus authority scope. Network scope has a stable configured network ID; local scope includes backend identity and persistent world-set identity rather than display names. The authority transaction checks the admission ID, snapshots previous visit fields, commits first/returning classification and active visit/session, selects variants and creates event IDs. A successfully admitted UUID becomes a known returning visitor after that commit even if no message reaches its client. Welcome and welcome-back are mutually exclusive for one visit; join remains an independent public family. A confirmed disconnect closes only the matching active session once, records a completed visit end and schedules its leave family after presence removal. Stale disconnect from an old backend/session cannot close a replacement session. Kicks count as confirmed leaves without exposing private moderation reasons in default text. Graceful shutdown suppresses mass leave announcements while recording confirmed visit closures; restart never broadcasts synthetic historical departures. An unclean crash leaves an uncertain end and cannot invent a completed-visit timestamp.

Before every delivery, recheck the subject's vanish policy and each recipient's current permission/session. For a leave after the subject has gone, retain its last authoritative visibility state and combine it with current observer rights; unknown visibility suppresses public output. Unauthorized observers receive no lifecycle text or hover for vanished subjects, and their visible count excludes hidden players. Welcome to the subject remains allowed. Staff audience does not itself grant hidden-activity access. Disconnection, permission revocation or session replacement before dispatch suppresses that recipient's claim without rerouting to a new session.

#### Delivery, reload, preview and migration

Delivery claims use event ID, family and recipient UUID/session as their unique key. Commit a claim before sending the native system component, then record submission to the platform transport. The word submitted does not assert that the client displayed or read the message. Replayed events observe the existing claim and do not send again. The bridge/backend delivery endpoint also commits a receipt key before forwarding any component. Normal transient transport failures may retry only while both sides prove no dispatch occurred; a failure or crash after claim/dispatch ambiguity records UNCERTAIN and is not replayed as a fresh announcement. Client display and durable storage cannot be committed atomically. A crash can therefore omit a message, but recovery must not invent a new first visit or duplicate an uncertain historical announcement.

Claims expire after ten seconds from their live lifecycle event. Persist detailed delivery claims for 24 hours, then compact them into durable closed event/visit fences; do not erase the active session's uniqueness fence. Reject any historical replay whose visit/session or event deadline is no longer current even after claim compaction. Limit pending delivery to 4096 recipient claims per process and 128 submissions per tick with at most one ms owner-thread work. Fan-out runs in bounded batches; overflow or expiry records suppressed/degraded delivery and never stalls login or retries indefinitely. Mandatory audit capture of those outcomes remains independent once Phase 009 supplies its adapters. The already-delivered diagnostic interface covers Phase 006 without requiring the future comprehensive journal.

Compile the complete proposed main-config subsection into an immutable snapshot before an atomic generation swap. Invalid reload leaves the last valid generation, selections and runtime policy unchanged and reports the exact key, variant, line and error without exposing private replacement values. The existing root config transaction handles schema validation and rollback. In-flight lifecycle events keep their selected template generation; new events use the new one. Audience and visibility authorization are always current, even for an older template generation. A newly disabled family cancels its unsent claims. On invalid startup, use a persisted verified last-good snapshot if available; otherwise keep lifecycle sending disabled, report the configuration failure prominently and preserve server operation. A newly generated valid main config uses the defaults above. Do not fall back to an unfiltered vanilla announcement.

Proposed administrative commands are `sef messages validate`, `sef messages reload`, `sef messages status` and, for example, `sef messages preview welcome greeting self`. Validation/reload requires `sef.config.reload`; preview requires `sef.messages.preview`; history inspection requires `sef.messages.history`. Console may validate/reload/status without a player and receives a plain preview with a synthetic sample subject unless a permitted real UUID is explicitly supplied. Preview sends only to the requester, is visibly labeled Preview, uses the current viewer's permissions, and neither broadcasts nor changes visit, selection or delivery state. Unknown variant, subject, placeholder or disabled authority returns safe rich feedback. The preview of a disabled family is allowed for an authorized administrator but remains marked disabled.

Migration scans supported existing SEF/player UUID records off the server thread before enabling classification. Seed proven existing UUIDs as KNOWN_RETURNING and preserve verified historical timestamps; absent or ambiguous timestamps stay null. Existing first-join reminder state can establish that a player was known, but cannot establish an exact completed-visit time. Never resolve identity by mutable display name alone. For network mode, import the union of validated backend UUID inventories through the same fenced authority migration used for other network data, with counts, source digests and conflict report. A first recorded visit means the first successful admission not already known in that verified imported baseline; it does not claim the player never visited an unrecorded historical server. Preserve source records and never dispatch historical welcomes during import.

Translate supported legacy `player`, `username`, `uuid` and `world` placeholders through explicit mappings to `player_display_name`, `player_name`, `player_uuid` and `dimension`. Legacy `player` retains the formatted display-name distinction while stripping interactive metadata; legacy `world` retains the full dimension resource key rather than substituting a world display label. Validate every translated template and keep unsupported constructs as inactive migration conflicts, without executing them or silently dropping the original. Main configuration always controls enabled family, audience, scope and limits. Retained per-player template customization, where proven by the action inventory, is available only when `allow_player_overrides=true`; it replaces join/leave variant text only and still uses the same compiler, policy and visibility. A valid per-player override is one deterministic variant identified by its template digest; it does not advance or reset the main family's saved variant sequence. Fresh configurations default false. An explicit supported migration can preserve a prior enabled override policy and lists affected UUID counts, never names in public output. First-join reminders remain separate retained reminder functionality with their own stable event IDs; they cannot masquerade as or duplicate the new welcome families.

Acceptance includes literal brace and unknown-placeholder compilation, bounds, Unicode/markup injection, seeded deterministic variant selection and restart persistence, list reorder/removal, invalid reload, preview authorization and no state mutation, first and returning UUIDs across name changes, migrated known users, missing history, actual post-admission messages, failed/denied login silence, rapid genuine reconnect, duplicate callbacks, bridge reconnect, transfer without extra welcomes, vanished subjects and per-recipient counts, authority outage, claim overflow/expiry, and crash before/after claim and submission. Use independent event counters and actual laptop chat for the residual display claim. Document exact defaults, schema, placeholders, previous-visit semantics, migration conflicts and crash-display limits in `docs/configuration/lifecycle-messages.md` and include all administrative result states in presentation coverage.

### State and protocol invariants

A world grid is a persistent immutable generation with exact inclusive integer bounds, parent rows/columns and per-parent child rows/columns. Require at least two parent cells and at least two children per parent for the selected boundary guards, positive finite counts, nonempty partitions, legal border intersection and at most 1,000,000 total child cells per grid. Use checked wide arithmetic and half-open boundaries `min + floor(i * width / count)` after converting max to max+1. Negative and uneven bounds partition exactly once. Draw x/z uniformly inside the chosen child, then find safe y. Section fairness is not area-weighted fairness when sizes differ.

Persist unbiased shuffled parent and child permutations, cycle IDs, committed/reserved sets and last actual committed entry. Each parent's child bag advances independently and survives every parent reshuffle. A parent allows one consuming reservation per parent cycle. A successful landing alone consumes both turns. Unsafe candidate rejection and proven non-arrival release provisional work; they never mark a turn used. An unavailable remaining cell can prevent completing a cycle, which is an honest unavailable result. A cycle advances only after all entries committed and no reservation remains ambiguous. At a new cycle, condition the first committed entry to differ from the preceding cycle's last actual landing. Because completion order differs from selection order, initially admit only the boundary-safe first reservation until it commits; subsequent concurrent reservations may then proceed. Apply the same child guard. Do not pre-reserve a new cycle based on guessed completion order.

Persist intent, arrival authorization, observed receipt, consumption and recent landing linkage. Audit spool rotation cannot discard this authority. Timeout after authorization quarantines; durable non-arrival evidence is required to release. Restart uses session/location evidence and exact world generation, never a seed reset. Topology or bounds changes create a new generation after draining proven cancellable work, preserving quarantines and requiring explicit confirmation that fairness history restarts. Ordinary reload does not reset bags.

Distance is horizontal XZ Euclidean; accept equality at the configured minimum and reject strictly smaller squared distance. Default minimum is 128 blocks and recent window 300 seconds, both typed configurable policy. Enforce online players including vanished players, active reservations and every successful landing in the live recent window. Recent state persists across restart, and a capacity of 100,000 entries per world uses admission backpressure rather than early eviction. It must not reveal hidden player identities or coordinates in failure text. Final checks on the server thread include border, build height, chunk identity/readiness, permissions, claims, safe footing, clearance, biome policy, liquids, fire, cactus, magma, powder snow and registered mod hazards. Recheck after warmup and transfer; an old prepared result is not authority.

Prepared candidates expire after 30 seconds, retain grid/policy/world/chunk revisions, and spend no turns. Default pool target is 32 candidates per world. Only eligible unused cells count as supply. Loaded chunks are the initial default; explicit bounded preparation can load/generate only after its supported API and cleanup prove safe. Initial limits are two ms or 16 checks per tick, four concurrent tickets per backend, 64 queue entries, ten second queue deadline, 32 coordinate attempts per request and 15 second preparation deadline. Refuse or queue within these limits without relaxing distance or fairness. Claim/world access remains on the owner thread; background workers perform only pure calculations.

Proxy routing first filters configured allowed destinations by compatible profile, fresh authenticated telemetry, moderation, readiness, world/grid eligibility and capacity. Samples arrive every two seconds using a ten second measurement window; exclude samples older than six seconds by local receipt time, unknown epochs, out-of-order sequences or missing factors. Rank by `0.35 * meanMspt/50 + 0.20 * spikeFraction + 0.25 * (players+incoming)/playerCapacity + 0.15 * chunkWork/chunkCapacity + 0.05 * (1-min(eligiblePrepared/preparedTarget,1))`, with bounded valid denominators and configured overload refusal at p95 MSPT above 50 or full capacity. Lower is better; ties use stable server ID, admission is serialized and incoming reservations immediately affect subsequent ranking. Ten percent hysteresis applies to retry preference only, never to overriding eligibility. World area is not a score input.

Admission leases last 15 seconds and renew every five while a bounded operation is healthy, with 30 seconds maximum before arrival authorization. Backend and destination reservation must both exist before switch. Uncertain arrivals retain capacity/quarantine until reconciliation; a clock expiry alone does not release them. Failover is permitted only before authorized/uncertain arrival with confirmed cancellation of earlier resources. Local mode never silently changes to proxy mode. These defaults require Phase 015 measured validation and documented tuning; they are not promised throughput.

## 12. Requirements

Each SEF-AC identifier below is the canonical acceptance identifier for its corresponding requirement. Phase tasks may add subordinate cases without changing this contract. All production verification is `none`; destructive exercises use disposable owned fixtures only.

### SEF-REQ-001 — Retained Forge backport

**Behavior:** Backport every retained implemented action to Minecraft 1.20.1, Forge 47.3.12 and Java 17; the delivered mod runs on a single Forge server without Velocity, Ambassador, ProxyCompatibleForge, the client reset mod or an SEF client requirement.
**Owner:** Forge backend  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-001  
**Dependencies:** SEF-REQ-002, SEF-REQ-010  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** MVP

**Acceptance criteria**

- SEF-AC-001. Phase 001 proves the native foundation, dedicated standalone startup without proxy adapters or client reset, no SEF client requirement and removal/readiness boundaries for subsequent adapters. Whole-inventory acceptance closes in Phase 015 after every retained action's implementation and evidence exists. No earlier phase depends on that future complete-inventory proof; each depends on the specific already-delivered native contract. At final acceptance every retained inventory row has a native implementation, applicable tests and no unresolved 1.21 API dependency.

**Required evidence**

- Phase 001 foundation inventory, compilation, Forge loader metadata and JAR class/resource inspection, dedicated startup without the external network adapters and actual compatible client login without client reset; Phase 003 retained local action evidence and Phase 015 final standalone whole-inventory closure.

### SEF-REQ-002 — Action-level source inventory

**Behavior:** Inventory implemented commands, policies, configuration, persistence, dependencies, integrations and tests at the pinned reference revision.  
**Owner:** Forge backend  
**Contributors:** Compatibility and repository evidence owners  
**Canonical implementation phase:** SEF-PHASE-000  
**Dependencies:** none  
**Lifecycle stage:** readiness  
**Production verification:** none  
**Release impact:** MVP

**Acceptance criteria**

- SEF-AC-002. Every discovered action is retained, adapted, economy removal, interface removal or unimplemented future scope, with exact source locator and dependency closure. Menu-dependent utilities and disguise paths have explicit disposition.

**Required evidence**

- CodeGraph map with bounded omission reads, action matrix and reference fingerprints; no executed feature inferred from a catalog name.

### SEF-REQ-003 — Complete economy removal

**Behavior:** Remove accounts, balances, payments, prices, reservations, worth, sales, shops/signs, market/trade money, monetary policy, providers, configuration, storage interfaces and dependencies.  
**Owner:** Forge backend  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-001  
**Dependencies:** SEF-REQ-002  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** migration

**Acceptance criteria**

- SEF-AC-003. No active construction, registration, command, permission, resource or dependency exposes money, including disabled modules. Existing owner data remains preserved and inert.

**Required evidence**

- Inventory removal closure, dependency graph/JAR inspection, command/config snapshots and migration fixture proving old files remain untouched.

### SEF-REQ-004 — Complete interface removal

**Behavior:** Remove custom screens, menus and menu-dependent utilities, HUDs, Fancy Tags, enhanced client networking/rendering, interface-only disguise, configuration, resources and tests.  
**Owner:** Forge backend  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-001  
**Dependencies:** SEF-REQ-002  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** migration

**Acceptance criteria**

- SEF-AC-004. No client initializer/class linkage, custom payload negotiation, custom screen/menu or replacement interface remains. Required vanilla chat, tab and visibility still work.

**Required evidence**

- Dedicated no-client-class startup, JAR inspection and real client without SEF; removal matrix covers kernel, config and assets.

### SEF-REQ-005 — Shared command policy

**Behavior:** Backport feature ownership, permissions, hierarchy, quotas, cooldowns, warmups, confirmations, identity, audit hooks, conflicts and bounded aliases/bundles without money or interface dependencies.  
**Owner:** Common command kernel  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-002  
**Dependencies:** SEF-REQ-001, SEF-REQ-003, SEF-REQ-004, SEF-REQ-006, SEF-REQ-019  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** MVP

**Acceptance criteria**

- SEF-AC-005. Every entry resolves one owner, checks source/target authority, bounds expansion and recursion, and reports actual terminal results. Console-safe actions do not require a player.

**Required evidence**

- Policy unit and real dispatcher tests for denial, hierarchy, nested alias limits, warmup cancellation, conflicts and exactly-once action effects.

### SEF-REQ-006 — Backend data preservation

**Behavior:** Use versioned retained data, atomic writes, explicit migration, corruption quarantine, backups and bounded shutdown while preserving old owner files.  
**Owner:** Backend persistence  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-001  
**Dependencies:** SEF-REQ-002, EXT-009  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** migration

**Acceptance criteria**

- SEF-AC-006. Supported config/player/location records migrate with counts and checksums; unsupported 1.21 item/world data is refused without destructive reinterpretation. Recovery reproduces identities and revisions.

**Required evidence**

- Interrupted writes, corrupt/unknown schema fixtures, restore drill and actual SQLite native/WAL/backup/restart tests on Java 17.

### SEF-REQ-007 — Retained command families

**Behavior:** Backport implemented chat, messaging, nicknames, mail, announcements, moderation helpers, kits, direct item/player utilities and compatible server controls.  
**Owner:** Forge gameplay adapters  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-003  
**Dependencies:** SEF-REQ-005, SEF-REQ-030  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** MVP

**Acceptance criteria**

- SEF-AC-007. Every retained action runs without excluded menus/economy, respects command policy and has natural product feedback. Missing provider implementations remain excluded future scope.

**Required evidence**

- Action inventory to dispatcher and GameTest coverage, persistence/reload checks, targeted real chat/input evidence for residual client claims.

### SEF-REQ-008 — Local safe teleportation

**Behavior:** Retain homes, warps, spawn, back and teleport safety under backend authority.  
**Owner:** Forge teleport service  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-003  
**Dependencies:** SEF-REQ-005, SEF-REQ-006, SEF-REQ-030  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** MVP

**Acceptance criteria**

- SEF-AC-008. Quotas, permissions, dimensions, border, world identity, hazards, warmup movement/damage/disconnect cancellation and back history operate on real successful movement only.

**Required evidence**

- Dedicated world fixtures and natural command paths, invalid dimensions/borders and cancellation tests, exact destination and facing assertions.

### SEF-REQ-009 — Independent Velocity companion

**Behavior:** Build a small Java 25 plugin on velocity-latest with isolated toolchain/artifact and exact shared contract provenance. The companion is installed only for the configured Ambassador network profile; standalone Forge never asks for it.
**Owner:** Velocity module  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-004  
**Dependencies:** SEF-REQ-010, SEF-REQ-005, EXT-002, EXT-003  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** MVP

**Acceptance criteria**

- SEF-AC-009. Proxy starts on pinned Velocity, loads no Forge/Minecraft world code, and pairs by protocol/common digest with the approved backend. Branch lineage follows Section 11. The operator guide identifies the separately downloadable SEF Velocity companion and its verified compatible artifact, rather than treating Ambassador alone as the companion.

**Required evidence**

- Proxy startup, API compilation, dependency/JAR inspection, paired digest check and required branch integration evidence.

### SEF-REQ-010 — Early real proxy feasibility

**Behavior:** Prove selected Forge handshake, authenticated forwarding, signed chat, command arguments and backend switching before dependent SEF network implementation. This network readiness gate does not make its external adapters a standalone installation requirement.
**Owner:** Compatibility fixtures  
**Contributors:** Compatibility and repository evidence owners  
**Canonical implementation phase:** SEF-PHASE-000  
**Dependencies:** EXT-001, EXT-003, EXT-004, EXT-005, EXT-010
**Lifecycle stage:** readiness  
**Production verification:** none  
**Release impact:** MVP

**Acceptance criteria**

- SEF-AC-010. Real matching Forge 1.20.1 client with the exact pinned reset adapter logs in through proxy, retains correct UUID, signs chat successfully, executes representative mod command arguments and switches in-session between dedicated Forge backends in both directions. Forged direct identity is rejected. Installed proxy and client adapter identities are separately observed. A reconnect after disconnection is not a successful switch.

**Required evidence**

- Pinned independent adapter fixture with proxy/backend/client logs, actual silent laptop interaction, denied spoof attempt, observed clientresetpacket capability, two-way in-session transfer and a diagnostic missing-capability control; downloaded bytes or manual reconnect alone fail acceptance.

### SEF-REQ-011 — Authenticated empty-backend bridge

**Behavior:** Provide independent authenticated private transport with peer/session/epoch/revision fencing, bounded work, replay rejection, version negotiation and rotation. When Ambassador network mode is configured but the SEF Velocity companion cannot be verified, expose a safe operator-only installation prompt and fence network operations without affecting standalone mode.
**Owner:** Network transport  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-004  
**Dependencies:** SEF-REQ-009, SEF-REQ-019  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** MVP

**Acceptance criteria**

- SEF-AC-011. Messages operate with zero connected players. Forged, replayed, stale, oversized and unknown-type traffic cannot mutate state; reconnect and key rotation converge without secret exposure or console relay. With network mode selected and no verified SEF Velocity companion, startup and an admin-only status command report that the matching companion must be installed on Velocity, identify its trusted artifact location and compatibility version from the approved manifest, and distinguish unverified installation from proven absence. A clickable download action is offered only when a verified HTTPS release location exists; otherwise the notice points to packaged installation instructions. They do not request any download in standalone mode, auto-download code, disclose secrets or present a network feature as ready.

**Required evidence**

- Empty-backend integration, captured/replayed frames, malformed lengths, stale boots, revoked keys, queue saturation, worker/thread assertions and restart; configured-network missing-companion, unreachable-companion and standalone-no-prompt fixtures with console and admin-only status transcripts.

### SEF-REQ-012 — Durable network authority

**Behavior:** Persist network bans, absolute mute expirations, vanish, qualified homes and operations in one proxy-owned SQLite writer with ordered replicas.  
**Owner:** Proxy authority  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-005  
**Dependencies:** SEF-REQ-011, SEF-REQ-006, EXT-009  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** migration

**Acceptance criteria**

- SEF-AC-012. Commit precedes publication, duplicate writes are idempotent, gaps resync atomically, restart preserves expiration/state, and authority outage refuses new unsafe operations. No shared database file exists.

**Required evidence**

- Concurrent CAS, snapshot gap/reorder/duplicate tests, crash cuts, WAL backup/restore, Java 25 native loading and empty-backend convergence.

### SEF-REQ-013 — Presence and command authority

**Behavior:** Resolve authenticated UUID/session location and route commands with consistent observer, source and target authorization.  
**Owner:** Network authority  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-005  
**Dependencies:** SEF-REQ-012, SEF-REQ-005  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** MVP

**Acceptance criteria**

- SEF-AC-013. Reconnect replaces epochs, stale source/target sessions cannot receive actions, and console/delegated permissions never acquire unintended player privileges. Visibility precedes suggestions and resolution.

**Required evidence**

- Rapid reconnect/switch and offline target fixtures, privilege revocation between dispatch/apply, delegated actor denial and correlation traces.

### SEF-REQ-014 — Network moderation and signed chat

**Behavior:** Apply bans, pardons, kicks, disconnects, mutes and unmutes to online and offline targets; proxy rejects banned login and Forge enforces signed chat/message paths.  
**Owner:** Network moderation  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-006  
**Dependencies:** SEF-REQ-013, SEF-REQ-007  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** MVP

**Acceptance criteria**

- SEF-AC-014. Mute survives switches/restarts to its absolute expiry; supported aliases and private/public command paths cannot bypass it. Unmuted signed chat remains valid, and disconnect outcome is distinguished from committed ban.

**Required evidence**

- Real signed-chat send/deny/expiry, offline ban then login, switch/reconnect and command alias tests; no proxy chat cancellation substitution.

### SEF-REQ-015 — Observer-aware network vanish

**Behavior:** Persist vanish and apply it before public visibility on join/switch, with observer permission checks across entities, tab, lists, selectors, resolution and suggestions.  
**Owner:** Network visibility  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-006  
**Dependencies:** SEF-REQ-013, SEF-REQ-014  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** MVP

**Acceptance criteria**

- SEF-AC-015. Ordinary observers cannot discover vanished targets including tp completion; authorized staff can. Supported map/player-list hooks have pinned evidence; unsupported hooks are explicit gaps.

**Required evidence**

- Two real observer roles on laptop, tab/entity and suggestion assertions, permission changes/restarts/switches, packet recipient checks and support matrix.

### SEF-REQ-016 — Qualified homes and transfer receipts

**Behavior:** Persist server/world/dimension/coordinates/facing and stable home revisions, and execute reserved correlated cross-server home travel.  
**Owner:** Network travel  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-007  
**Dependencies:** SEF-REQ-008, SEF-REQ-018, SEF-REQ-015  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** MVP

**Acceptance criteria**

- SEF-AC-016. Local duplicate names reject, remote duplicates require deterministic choice, deleted/renamed/stale homes do not act, target joins intended session before one safe destination mutation, and only a durable arrival yields success.

**Required evidence**

- Three-backend home matrix including identical names/dimensions, world replacement, unsafe destination, disconnect, replay, timeout and restart at each transfer stage.

### SEF-REQ-017 — Consent-aware network teleports

**Behavior:** Route retained player teleport and request workflows using current session, observer visibility, consent, warmup and cancellation.  
**Owner:** Network teleport coordinator  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-007  
**Dependencies:** SEF-REQ-016  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** MVP

**Acceptance criteria**

- SEF-AC-017. Acceptance binds both participants and requested action; target moves, disconnects, changes permission or loses visibility trigger correct revalidation/refusal. Back history reflects actual movements.

**Required evidence**

- Real tpa accept/decline/expiry, recipient-token theft, concurrent target switch, warmup cancellation and qualified back round trip.

### SEF-REQ-018 — Validated modpack relations

**Behavior:** Support the three matching packs and explicitly verified compatible differing profiles. In the approved Ambassador Forge proxy profile, require an observed client reset capability before in-session movement; reject missing, unknown or incompatible capabilities and relations before moving the player. Standalone local behavior has no proxy, PCF or client-reset prerequisite.
**Owner:** Proxy compatibility registry  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-005  
**Dependencies:** SEF-REQ-010, SEF-REQ-013  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** MVP

**Acceptance criteria**

- SEF-AC-018. Profiles bind mod/config/registry/adapter and reset-capability digests with direction-specific real evidence. Missing reset capability, unknown or incompatible choice leaves source session/world state unchanged with a clear installation or client-pack explanation.

**Required evidence**

- Pinned same-profile and differing-compatible real transfers using EXT-010, deliberately absent reset capability, incompatible registries/arguments and stale profile evidence refusal; no SEF client module or launcher.

### SEF-REQ-019 — Configuration and usable diagnostics

**Behavior:** Deliver modular validated server configuration, atomic reload, bounded default-off diagnostics, correlation and support recovery before dependent tests.  
**Owner:** Common foundation  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-001  
**Dependencies:** SEF-REQ-002  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** MVP

**Acceptance criteria**

- SEF-AC-019. Exact proposed on/status/off controls work from console with permission checks and ten-field diagnostic contract; invalid reload preserves active config; later modules register typed categories under the same interface.

**Required evidence**

- Enable/status/disable, timeout/reload/restart, missing target, redaction, queue/output budgets and overhead tests plus delivered support runbook rehearsal.

### SEF-REQ-020 — Complete topology verification

**Behavior:** Verify complete standalone Forge operation without external proxy adapters, then normal, denied, forged, replayed, stale, concurrent, empty, offline, switch, restart, partial commit, storage failure and recovery across one proxy and three backends.
**Owner:** Integrated verification  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-015  
**Dependencies:** SEF-REQ-035, SEF-REQ-028, SEF-REQ-015, SEF-REQ-036, EXT-001  
**Lifecycle stage:** post_change  
**Production verification:** none  
**Release impact:** stable release

**Acceptance criteria**

- SEF-AC-020. The final Forge artifact starts and serves retained local commands, storage, homes, lifecycle messages, audit and local RTP with no proxy process, PCF backend mod or client reset mod installed and no companion installation prompt. With Ambassador network mode selected, the absent or unreachable SEF Velocity companion yields a safe operator-only install or connection diagnostic and network operations remain unavailable. Network scenario gates use actual supported artifacts and exact hosts; server assertions prove server claims and silent real clients prove residual input/rendering/synchronization. Cleanup is complete everywhere.

**Required evidence**

- Versioned standalone and network scenario matrices, Forge loader metadata and dependency scans, paired artifact hashes, decisive logs/screenshots and fault cut results, thread/tick budgets and per-host cleanup receipts.

### SEF-REQ-021 — Complete operator and developer documentation

**Behavior:** Document separate standalone and Ambassador network installation paths, registration, forwarding, commands, permissions, configuration, migrations, backup, rollback, compatibility, APIs, support and paired artifacts.
**Owner:** Documentation  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-016  
**Dependencies:** SEF-REQ-020, SEF-REQ-027  
**Lifecycle stage:** post_change  
**Production verification:** none  
**Release impact:** stable release

**Acceptance criteria**

- SEF-AC-021. README, docs index, technical overview and every affected topic match implemented verified behavior. A one-server operator needs only the Forge artifact among SEF runtime components and can configure the separately documented audit storage without proxy adapters, a client reset mod or a companion prompt. The Ambassador network guide lists the separately installed proxy, backend and client adapters, their exact pins, the downloadable SEF Velocity companion from a verified artifact location, missing-companion diagnosis and missing-client-capability refusal. Both SQL engines, loss policy, previews, RTP fairness and mismatch recovery are usable without undocumented steps.

**Required evidence**

- Clean-install and runbook rehearsal against final artifacts, documentation/link checks and tracked-to-wiki post-merge correspondence.

### SEF-REQ-022 — Checked sequential integration and artifacts

**Behavior:** Complete every applicable checked PR merge, resulting product-branch verification, signed phase tag and paired artifact provenance.  
**Owner:** Repository integration  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-016  
**Dependencies:** SEF-REQ-021, EXT-002  
**Lifecycle stage:** post_change  
**Production verification:** none  
**Release impact:** stable release

**Acceptance criteria**

- SEF-AC-022. All phases integrate sequentially with EnVy sole signed authorship; both required product PRs close before progression. Artifacts include source commits, common/protocol digests, SHA256/SHA512, licenses and SBOM. No production/public publication occurs.

**Required evidence**

- GitHub merge/check evidence, fetched branch containment and signature verification, final rebuilt artifact manifests and retained historical branch/tag checks.

### SEF-REQ-023 — Comprehensive applied activity capture

**Behavior:** Capture all declared server-observable action families with actual outcomes, before/after state, actor/cause confidence and explicit mod coverage.  
**Owner:** Forge audit adapters  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-009  
**Dependencies:** SEF-REQ-024, SEF-REQ-026  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** MVP

**Acceptance criteria**

- SEF-AC-023. Coverage matrix includes every family in Section 11; canceled/simulated actions never become applied, same-tick changes remain distinct, movement records accepted changes without sampling, and opaque state/gaps are visible.

**Required evidence**

- Real mutation entry-point tests with independent world/inventory oracles, vanilla and pinned mod adapters, fake-player and cause-context isolation, exact coverage inventory.

### SEF-REQ-024 — Durable independent audit storage

**Behavior:** Provide durable segmented local journals and deduplicated central MySQL/MariaDB history separate from network authority.  
**Owner:** Audit storage  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-008  
**Dependencies:** SEF-REQ-006, SEF-REQ-019, SEF-REQ-011, EXT-006, EXT-007, EXT-008  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** migration

**Acceptance criteria**

- SEF-AC-024. Stable origin/epoch/sequence, actual snapshot versions and four watermarks survive retries/restart. Offline-backend replicated history stays queryable. Corrupt/torn/uncertain tails are explicit and SQL never blocks ticks.

**Required evidence**

- Both actual SQL engines: duplicate/deadlock/remote-commit-before-ack, local fsync cuts, corruption, outage/replay, schema/backup/restore and pool-isolation tests.

### SEF-REQ-025 — Scoped inspection, queries and export

**Behavior:** Deliver permission-controlled click inspection and filtered stable-page lookup, event/cause detail, near/count/material summaries and bounded exports.  
**Owner:** Audit investigation service  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-010  
**Dependencies:** SEF-REQ-023, SEF-REQ-031, SEF-REQ-030  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** MVP

**Acceptance criteria**

- SEF-AC-025. All Section 11 filters apply to every page/detail/hover/export; revoked rights deny continuation; no custom menu or unintended block mutation. Empty results display coverage/watermark uncertainty.

**Required evidence**

- Inspector natural click and cancellation, query snapshots during ingest, SQL index plans, sensitive/hidden read denial, offline backend history and malicious filter/export-path cases.

### SEF-REQ-026 — Content privacy, retention and recovery

**Behavior:** Retain full public/private communications and arguments with credential redaction before all sinks, separate sensitive rights, configurable normal retention with manual purge by default and an explicitly authorized optional schedule, and visible availability-first loss policy.
**Owner:** Audit storage policy  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-008  
**Dependencies:** SEF-REQ-024  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** migration

**Acceptance criteria**

- SEF-AC-026. Known credential schemas and typed sensitive fields never reach any sink; unknown unsafe content carries redaction reason. Only oldest closed owned audit segments rotate, gaps persist and central SQL is not silently purged. Retention defaults to manual. Scheduled deletion requires confirmed policy scope, frozen selections, a fenced single executor and current restoration-dependency protection; invalid reload, revocation, migration or uncertainty cannot bypass those guards.

**Required evidence**

- Secret sentinel scan across memory/spool/SQL/export/alerts, full storage and loss-ledger failure injection, symlink/unrelated-file protection, manual purge preview/confirm and backup recovery. On both SQL engines, prove disabled scheduling across restart, explicit activation, invalid reload preservation, duplicate executor fencing, dependency and migration races, crash recovery, cancellation, durable purge boundaries and standalone operation without Velocity.

### SEF-REQ-027 — Audit coverage and conservation proof

**Behavior:** Prove declared capture, query, restoration and state conservation under real vanilla/modded workloads and both SQL engines.  
**Owner:** Integrated audit verification  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-015  
**Dependencies:** SEF-REQ-020, SEF-REQ-028, SEF-REQ-029  
**Lifecycle stage:** post_change  
**Production verification:** none  
**Release impact:** stable release

**Acceptance criteria**

- SEF-AC-027. Hopper/machine/container/crafting flows conserve both ends; canceled and duplicate callbacks remain honest; crash/storage/query pressure exposes every known gap. Published overhead and limitations match actual evidence.

**Required evidence**

- Independent fixture counts, event and job cut-point matrix, mixed causes, movements, representative modpack workload p95/p99 metrics, dual-engine restore and capacity results.

### SEF-REQ-028 — Safe rollback, restore and undo

**Behavior:** Provide fixed-selection previews and confirmed bounded restoration for declared reversible types with durable partial jobs and conservative conflict handling.  
**Owner:** Backend restoration executors  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-011  
**Dependencies:** SEF-REQ-025, SEF-REQ-029  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** MVP

**Acceptance criteria**

- SEF-AC-028. No live edit is overwritten without matching preimage; both item endpoints and onward dependencies are accounted; unknown steps quarantine; preview cleanup restores display; repeated apply/undo cannot duplicate. Give remains distinct and privileged.

**Required evidence**

- Real block/entity/container/player inventory restoration, full destination, onward crafting, removed registry, offline inventory, repeated actions and crash after world mutation before acknowledgment on both engines.

### SEF-REQ-029 — Versioned functional and API parity

**Behavior:** Enumerate and satisfy versioned CoreProtect-style capture, inspection, queries, filters, pages, summaries, preview/rollback/restore/undo, purge, extension and operational functions through original Forge equivalents.  
**Owner:** Audit API  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-010  
**Dependencies:** SEF-REQ-025  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** MVP

**Acceptance criteria**

- SEF-AC-029. Every applicable row maps to an owned implementation and acceptance; Phase 011 closes restoration rows and Phase 015 verifies full parity. Version/platform N/A has reason and evidence, feasible functions are not removed as inconvenient.

**Required evidence**

- CE24.1/catalog/API matrix, original adapter contract tests, queue/version/pause/resume/status tools, MySQL-to-MariaDB and reverse fenced migration drills, final row closure.

### SEF-REQ-030 — All-command rich vanilla presentation

**Behavior:** Render every retained and new command through original localized semantic messages matching supplied calm colors, check/cross brackets, grouped details, hovers and pagination.  
**Owner:** Common presentation  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-002  
**Dependencies:** SEF-REQ-005  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** MVP

**Acceptance criteria**

- SEF-AC-030. Every command/result inventory row uses both platform adapters; literal malicious values cannot inject actions; recipient/session/revision tokens expire and reauthorize; destructive operations confirm; console/accessibility remain readable. Phase 015 checks all later commands.

**Required evidence**

- Component semantic snapshots and malicious Unicode/markup inputs, token replay/revocation, localization/console checks and real laptop screenshots/actions against SRC-007 and SRC-008.

### SEF-REQ-031 — Operational capacity and gap alerts

**Behavior:** Expose capacity/loss/recovery incidents to all authorized operators/admins locally and through optional configured Discord summaries.  
**Owner:** Audit incident service  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-008  
**Dependencies:** SEF-REQ-026  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** MVP

**Acceptance criteria**

- SEF-AC-031. Warnings precede exhaustion thresholds; local and central failures differ; deduplicated bounded retry honors response limits, protects URL/secrets and remains locally visible after webhook failure.

**Required evidence**

- Restricted health/incident command tests, mock webhook 429/outage/recovery, secret/mention/redirect denial and durable gap visibility through restart.

### SEF-REQ-032 — Persistent hierarchical RTP allocation

**Behavior:** Persist independent shuffled parent and child cycles, exact partitions and durable provisional reservations; consume only successful landings.  
**Owner:** Backend grid authority  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-012  
**Dependencies:** SEF-REQ-006, SEF-REQ-024  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** MVP

**Acceptance criteria**

- SEF-AC-032. Six parents with 20 by 20 children yield 2400 distinct cells. Each parent appears once per completed cycle, boundary actual landings never repeat immediately, child cycles survive parent reshuffles and crash recovery, invalid layouts reject.

**Required evidence**

- Deterministic property tests across negative/extreme/uneven bounds, completion permutations, at least 400 parent cycles, duplicate IDs, crashes at every state and generation migration.

### SEF-REQ-033 — Strict RTP separation and safe local arrival

**Behavior:** Enforce configured XZ distance from online/vanished players, reservations and persistent recent arrivals plus claims/borders/chunks/hazards at final mutation.  
**Owner:** Forge RTP safety  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-013  
**Dependencies:** SEF-REQ-032, SEF-REQ-008  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** MVP

**Acceptance criteria**

- SEF-AC-033. No successful arrival violates any constraint; unsafe/exhausted queues return bounded unavailable without cycle reset, burned failed turns or reduced distance. Local mode requires no proxy and releases all tickets on terminal outcomes.

**Required evidence**

- Dedicated actual safe/unsafe landings, adjacent-cell/vertical/vanish distance, claim changes during warmup, player races, nether/end hazards, chunk preparation cancellation and no-proxy startup.

### SEF-REQ-034 — Optional load-aware proxy RTP

**Behavior:** Choose eligible compatible backend from authenticated fresh normalized load before using destination grid; reserve admission and destination before switching.  
**Owner:** Network RTP  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-014  
**Dependencies:** SEF-REQ-033, SEF-REQ-017, SEF-REQ-018  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** MVP

**Acceptance criteria**

- SEF-AC-034. Every required load factor influences selection, world size does not; stale/unready/forged sources reject; serialized leases prevent stampedes; arrival receipts alone determine success and uncertainty fences failover.

**Required evidence**

- Unequal capacities and changing load, zero/stale/replayed samples, burst admission, destination restart/switch failure, one-proxy three-backend real arrivals and local-mode independence.

### SEF-REQ-035 — RTP lifecycle, budgets and recovery

**Behavior:** Provide validated generations, explicit topology migration, bounded preparation/work, scoped diagnostics, durable recovery and complete local/proxy documentation.  
**Owner:** RTP operations  
**Contributors:** Native backend and proxy adapters where the contract crosses platforms  
**Canonical implementation phase:** SEF-PHASE-014  
**Dependencies:** SEF-REQ-034, SEF-REQ-019  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** migration

**Acceptance criteria**

- SEF-AC-035. Reload never silently resets cycles or truncates recent windows; configuration changes fence old work, uncertain arrivals remain quarantined, work/queue/ticket limits hold and diagnostics explain decisions without private locations.

**Required evidence**

- Migration and restart equivalence, persistence failure, recent-ledger capacity backpressure, measured preparation and routing limits, both-mode operator runbook and final real landing evidence.

### SEF-REQ-036 — Configurable lifecycle messages

**Behavior:** Provide editable main-config join, leave, first-time welcome and welcome-back message families with multiple variants and safe placeholders including {player_name}. Use the common HuskHomes-style native chat renderer, independently enabled bounded templates, documented audiences and variant policies, a typed placeholder catalog, durable UUID-based first/returning visit semantics, single local/network lifecycle ownership, transfer and reconnect deduplication, permission-aware vanish filtering, atomic validated reload with last-good preservation, restricted preview, migration, diagnostics and real local/network acceptance. Do not add a graphical editor, scripts, arbitrary console actions or a required client component.  
**Owner:** Lifecycle messaging  
**Contributors:** Common configuration and renderer, proxy authority, Forge lifecycle and visibility adapters  
**Canonical implementation phase:** SEF-PHASE-006  
**Dependencies:** SEF-REQ-006, SEF-REQ-013, SEF-REQ-015, SEF-REQ-019, SEF-REQ-030  
**Lifecycle stage:** change  
**Production verification:** none  
**Release impact:** migration

**Acceptance criteria**

- SEF-AC-036. The canonical main configuration edits independently enabled join, leave, first welcome and welcome-back families, their bounded variants, typed safe placeholders including `{player_name}`, documented audiences and durable no-immediate-repeat or round-robin selection. Shared rich native rendering, literal braces, unavailable values, console/accessibility and restricted preview follow the exact Section 11 contract.
- Standalone backend and network proxy each have one explicit authority. First/returning classification and previous completed visit use durable UUID records and an imported baseline. Announcements follow successful admission; failed login and backend switch generate no extra welcome, while genuine new reconnects generate one new returning visit. Duplicate/reordered callbacks cannot repeat delivery claims or overwrite a newer session.
- Visibility filters the subject, recipients, hover and online counts. Reload atomically preserves the last valid generation on error. Preview changes no visit or selection state. Supported legacy templates/reminders migrate with documented precedence, original data preservation and explicit inactive conflicts.
- Normal repeat delivery is deduplicated. Crash uncertainty between durable claim and client display remains explicit, expires without replaying historical announcements and cannot reclassify a known UUID as first-time. Queue/output limits fail visibly without blocking gameplay or claiming display success.

**Required evidence**

- Compiler/property and actual dispatcher tests for every template limit, placeholder, brace/control/markup input, deterministic variant/reload state, preview permission and invalid main-config behavior.
- Dedicated local and real one-proxy three-backend admission/leave/transfer/reconnect tests with independently observed UUID visit records, stale-session and vanish roles, prior timestamps, imports, name changes, duplicate events, authority outage and every delivery crash cut. Silent laptop chat evidence proves actual native presentation, including per-recipient count privacy.
- Phase 006 delivers the complete feature and documentation; Phase 015 revalidates it with the final paired artifacts, all later transfer/RTP paths and the complete presentation inventory. All owned fixture, audio and process cleanup must pass.

## 13. Phased Roadmap

This is the sole global sequence. Each linked file contains the only full phase declaration and its ordered task blueprint. All previous phases must be integrated and tagged before the next starts, including both product integrations when applicable. Additional listed requirement dependencies express the specific technical gate, not permission to skip preceding phases. A phase cannot exit with a known mandatory phase-owned defect. Future consumers of shared requirements carry their own adoption gates and never reopen source scope by implication.

| Phase ID | Objective | Owner | Dependencies | Canonical requirements | Entry summary | Exit summary | Next transition | Execution blueprint |
|---|---|---|---|---|---|---|---|---|
| SEF-PHASE-000 | Freeze action inventory and prove exact external network feasibility | Compatibility readiness | EXT-001, EXT-002, EXT-003, EXT-004, EXT-005, EXT-010 | SEF-REQ-002, SEF-REQ-010 | Baseline identity and source evidence match | Complete disposition map, real handshake/identity/chat/command/switch proof and cleanup; blockers remain explicit | SEF-PHASE-001, start shared diagnostics | [Phase 000](phases/plan-phase-000.md) |
| SEF-PHASE-001 | Establish native server foundation, removals, safe data and diagnostics | Forge backend | SEF-PHASE-000, EXT-009 | SEF-REQ-001, SEF-REQ-003, SEF-REQ-004, SEF-REQ-006, SEF-REQ-019 | Inventory and feasibility approved | Diagnostics precede tests, standalone startup without proxy adapters passes, native foundation/removal/readiness gates pass and migrations preserve data; whole-inventory AC001 closure is Phase 015 after retained adapters, never this early exit | SEF-PHASE-002, implement command policy and renderer | [Phase 001](phases/plan-phase-001.md) |
| SEF-PHASE-002 | Deliver shared command policy and rich component/action system | Command platform | SEF-PHASE-001 | SEF-REQ-005, SEF-REQ-030 | Native foundation and diagnostic API merged | Policy and both render adapter contract fixtures pass; every then-existing command/result converted; subsequent registration requires presentation coverage | SEF-PHASE-003, port retained gameplay actions | [Phase 002](phases/plan-phase-002.md) |
| SEF-PHASE-003 | Complete retained local commands and safe teleport workflows | Forge gameplay | SEF-PHASE-002 | SEF-REQ-007, SEF-REQ-008 | Command policy, data and presentation approved | Every retained action implemented and verified, local teleport and recovery work with no excluded dependency | SEF-PHASE-004, create proxy lineage and bridge | [Phase 003](phases/plan-phase-003.md) |
| SEF-PHASE-004 | Build paired Velocity artifact and authenticated direct transport | Network transport | SEF-PHASE-003, EXT-002, EXT-003, EXT-004, EXT-005 | SEF-REQ-009, SEF-REQ-011 | External feasibility still matches pins and local backend passes | Java 25 proxy starts, common digest matches, bridge works with no players, forgery/replay/rotation bounded; both branches integrated | SEF-PHASE-005, persist network authority | [Phase 004](phases/plan-phase-004.md) |
| SEF-PHASE-005 | Deliver durable network state, sessions and pack admission | Proxy authority | SEF-PHASE-004, EXT-009, EXT-010 | SEF-REQ-012, SEF-REQ-013, SEF-REQ-018 | Bridge and pair provenance pass | Single-writer restart/resync, UUID epochs and same/different compatible relations proven; missing reset capability and incompatible transfer refused before movement | SEF-PHASE-006, enforce moderation and visibility | [Phase 005](phases/plan-phase-005.md) |
| SEF-PHASE-006 | Apply moderation, observer-aware vanish and configurable lifecycle messages | Network enforcement | SEF-PHASE-005 | SEF-REQ-014, SEF-REQ-015, SEF-REQ-036 | Authority, shared config/renderer and presence available; visibility precedes lifecycle dispatch | Real moderation/visibility and main-config local/network lifecycle semantics, variants, UUID visits, migration, privacy and crash-delivery limits pass | SEF-PHASE-007, implement qualified travel | [Phase 006](phases/plan-phase-006.md) |
| SEF-PHASE-007 | Deliver homes, consent-aware travel and durable arrivals | Network teleport | SEF-PHASE-006 | SEF-REQ-016, SEF-REQ-017 | Compatibility, visibility and local safety proven | Duplicate names, world identities, consent/session changes, actual destination receipts and uncertain recovery pass | SEF-PHASE-008, establish audit durability | [Phase 007](phases/plan-phase-007.md) |
| SEF-PHASE-008 | Establish journal/API, both SQL engines, privacy and incident delivery | Audit storage | SEF-PHASE-007, EXT-006, EXT-007, EXT-008 | SEF-REQ-024, SEF-REQ-026, SEF-REQ-031 | Identity, diagnostics, transport and authority isolation exist | Durable audit API before capture, dual-engine ingest/recovery, redaction, safe local rotation and mock alert paths proven | SEF-PHASE-009, attach real mutation adapters | [Phase 008](phases/plan-phase-008.md) |
| SEF-PHASE-009 | Capture comprehensive applied actions with explicit coverage | Forge audit adapters | SEF-PHASE-008 | SEF-REQ-023 | Journal and sensitive-state contracts verified | All capture families and supported pack adapters have independent real-path oracles, no silent sampling or false attribution | SEF-PHASE-010, expose scoped investigation and API | [Phase 009](phases/plan-phase-009.md) |
| SEF-PHASE-010 | Deliver investigation, parity registry, extension API and database migration | Audit investigation | SEF-PHASE-009 | SEF-REQ-025, SEF-REQ-029 | Actual capture and watermarks available | Inspector/filter/page/export/pause/status/API and migration paths pass; restoration parity rows are bound to Phase 011, not prematurely claimed complete | SEF-PHASE-011, implement fixed restoration jobs | [Phase 010](phases/plan-phase-010.md) |
| SEF-PHASE-011 | Deliver previewed, conservation-safe rollback, restore and undo | Backend restoration | SEF-PHASE-010 | SEF-REQ-028 | Query snapshots, typed snapshots and coverage proven | Fixed selection, both-end conservation, private preview cleanup, exact undo, privileged give and crash quarantine pass on both SQL engines | SEF-PHASE-012, implement persistent grid authority | [Phase 011](phases/plan-phase-011.md) |
| SEF-PHASE-012 | Implement exact partitions, independent cycles and durable reservations | Backend RTP authority | SEF-PHASE-011, EXT-009 | SEF-REQ-032 | Durable identity/storage and audit API available | Property/crash/concurrency tests prove partitions, successful-only consumption and actual-landing cycle barriers | SEF-PHASE-013, integrate local safety and preparation | [Phase 012](phases/plan-phase-012.md) |
| SEF-PHASE-013 | Deliver strict-distance safe local RTP with bounded preparation | Forge RTP service | SEF-PHASE-012 | SEF-REQ-033 | Allocation and local teleport contracts verified | Actual no-proxy landings, recent ledger, hidden-player distance, hazards, claims races and ticket cleanup pass | SEF-PHASE-014, integrate admission routing and lifecycle | [Phase 013](phases/plan-phase-013.md) |
| SEF-PHASE-014 | Deliver optional proxy routing, generation migration and operations | Network RTP | SEF-PHASE-013, EXT-010 | SEF-REQ-034, SEF-REQ-035 | Local safety and network arrival machinery pass | Fresh normalized routing, reset-capability admission fencing, three-backend arrivals, configuration generations and recovery proven | SEF-PHASE-015, run complete assurance matrix | [Phase 014](phases/plan-phase-014.md) |
| SEF-PHASE-015 | Prove all workflows, audit conservation, presentation and operating limits | Integrated verification | SEF-PHASE-014, EXT-001, EXT-003, EXT-004, EXT-005, EXT-006, EXT-007, EXT-010 | SEF-REQ-020, SEF-REQ-027 | All implementation phases integrated, paired candidates fixed | Final standalone and network profiles both pass; every product AC owned through Phase 015, including final AC001 and lifecycle AC036, passes at required fidelity; the complete 36-row catalog marks delivery AC021 and AC022 pending Phase 016; parity/presentation inventories close, measured limits and cleanup proven; no known mandatory defect | SEF-PHASE-016, close documentation and paired integration endpoint | [Phase 015](phases/plan-phase-015.md) |
| SEF-PHASE-016 | Complete operator delivery and verify final approved paired artifacts | Delivery integration | SEF-PHASE-015, EXT-002 | SEF-REQ-021, SEF-REQ-022 | Complete assurance packet and no unresolved mandatory defect | Final docs/runbooks, both applicable checked integrations, resulting branch verification, signatures, pair manifest, hashes/SBOM and all cleanup complete | Final plan-wide closure under Section 18 | [Phase 016](phases/plan-phase-016.md) |

The following task IDs are reserved as mandatory anchors in the corresponding blueprints: P000-TASK-001 inventory and source boundary; P000-TASK-002 external feasibility; P001-TASK-001 diagnostic implementation before consumers; P001-TASK-002 data/config/removal foundation; P002-TASK-001 command policy; P002-TASK-002 renderer/action tokens; P004-TASK-001 paired module/provenance; P004-TASK-002 bridge security; P005-TASK-001 authority/session/resync; P005-TASK-002 compatibility admission; P006-TASK-001 moderation; P006-TASK-002 visibility; P006-TASK-003 lifecycle configuration/visits/delivery/migration; P006-TASK-004 lifecycle acceptance and documentation; P007-TASK-001 homes; P007-TASK-002 transfer recovery; P008-TASK-001 journal/dual SQL; P008-TASK-002 privacy/rotation/alerts; P009-TASK-001 actual capture; P009-TASK-002 attribution/adapters; P010-TASK-001 queries/API/parity; P010-TASK-002 migration; P011-TASK-001 jobs/conservation; P011-TASK-002 preview/recovery; P012-TASK-001 grid/cycle properties; P012-TASK-002 durable reservations; P013-TASK-001 safety/distance; P013-TASK-002 preparation; P014-TASK-001 telemetry/admission; P014-TASK-002 generations/recovery; P015-TASK-001 complete topology/presentation; P015-TASK-002 audit/load assurance; P016-TASK-001 documentation rehearsal; P016-TASK-002 final pair/integration closure. Detailed phases may add unique local tasks while preserving these anchors and their ownership.

## 14. Verification Strategy

### Verification and evidence

Evidence binds actual inputs, candidate identity and required proof fidelity. Server claims use real headless paths and residual client claims retain laptop gates.

### Execution Hosts

| Workload or gate | Execution host | Required capabilities and launch configuration | Candidate identity and runtime directory | Evidence |
|---|---|---|---|---|
| Formatting, lint, unit/property tests, data/resource validation, packaging | node-1 or laptop | Inspect wrapper task graph first; no implicit client, renderer or display | Exact approved source and candidate hashes; discover existing checkout and use owned nested scratch | Commands, test counts, decisive results and cleanup |
| Dedicated Forge, server-only GameTests, proxy and SQL fixtures | node-1 | Actual no-GUI server task, controlled stdin, non-root private fixture services, eula=true readback | Owned nested runtime below verified Sef anchor; exact loader/JDK/mod/DB/artifact manifest | Readiness, real handler outcomes, bounded logs, process/ticket cleanup |
| Rendering, click input, chat actions, tab/entity visibility, actual client reconnect/switch | Verified Linux laptop | Active Hyprland, actual discrete NVIDIA renderer, supported connection controls, prelaunch silence and exact per-stream mute | Discover laptop project anchor independently; isolated instance with matching artifacts and pinned client profile | Window/PID/renderer/mute proof, client/server join correlation and targeted visual evidence |
| Network world workflow needing actual clients | Laptop plus node-1 | Ready dedicated private endpoint, authorized connection, required player roles, controllable server console | Same source/pair/protocol/hash/config matrix on both hosts | Joined intended player and world on both sides, action traces and residual client evidence |

Never run clients on node-1, including offscreen, Xvfb, VNC, X11 forwarding, software rendering or an integrated GPU workaround. For each laptop launch, set the isolated version's master audio to zero first. Bind the exact owned `hyprctl clients -j` window address, class, title and PID to the launched candidate. Correlate only that process tree with its PipeWire/PulseAudio playback node, mute it using `wpctl` or `pactl`, and verify the stream is muted before assertions. An owned bounded watcher rechecks resource reloads, device changes, reconnects and replacement streams. If identity, renderer or mute proof fails, stop the owned client and leave that gate open. Never change default sinks, microphones, other applications or personal instances. Teardown stops watcher/client, confirms streams disappeared and removes temporary audio state.

For actual in-world checks, confirm dedicated startup and the exact private endpoint before automatically connecting using the pinned launcher's supported options or authorized desktop controls. Prove the intended player entered the intended server world in both client and server evidence. Console fixtures establish preconditions but never bypass player permissions, natural input or the behavior under test. Singleplayer is allowed only for a named singleplayer/integrated-server bug with a recorded rationale on the laptop. Connectivity failure is not such a rationale. Continue independent headless work if EXT-001 lacks one capability.

### Evidence coverage and independent oracles

| Requirement family | Unit/property | Integration and real behavior | Security and failure | Final evidence |
|---|---|---|---|---|
| SEF-REQ-001 through SEF-REQ-008 | Catalog, codecs, migrations, policy | Real Forge dispatch and world actions, retained action inventory | Removal closure, corrupt data, denied hierarchy and cancellation | Per-action result and server-only artifact inspection |
| SEF-REQ-009 through SEF-REQ-018 | Typed codecs, revision/CAS and state machines | Real login/chat/transfer, zero-player bridge, three backend topology | Forgery, replay, stale sessions, incompatible pack, key rotation and outages | Correlated proxy/backend/client receipts and visibility matrix |
| SEF-REQ-019 and SEF-REQ-030 | Config, diagnostic limits, semantic component snapshots and tokens | Console support procedure and actual laptop text/actions | Injection, revoked permission, stale recipient/target, secret sentinel and overhead | Every command/result inventory row and screenshot-reference comparison |
| SEF-REQ-023 through SEF-REQ-029 and SEF-REQ-031 | Event/schema/redaction/query/job invariants | Real mutations, paired inventories, both SQL engines, preview and restoration | Simulation/cancellation, duplicates, crashes, disk pressure, corruption, privacy and unsafe paths | Coverage/parity matrices, watermarks/gaps and conservation counts |
| SEF-REQ-032 through SEF-REQ-035 | Exhaustive partitions, permutation and restart properties | Real safe landings with/without proxy, claims/hazards and chunk work | Concurrent completion order, uncertainty, stale telemetry and admission bursts | Allocation ledger, arrival receipts, strict distance checks and measured budgets |
| SEF-REQ-036 | Template compiler, variant policy, UUID visit state and claims | Actual local/network login, leave, transfer silence, native chat and preview | Invalid reload, vanished subject/counts, duplicate callbacks, old sessions, claim crash ambiguity and migration | Main-config examples, record/counter oracles and final artifact chat evidence |
| SEF-REQ-020, SEF-REQ-021, SEF-REQ-022, SEF-REQ-027 | Manifest/schema/link checks | Complete candidate matrix and operator rehearsals | Evidence invalidation and final integration rechecks | Approved product commits, signed tags, checksums, SBOM and cleanup receipts |

Tests inspect actual handler entry points. Inventory conservation uses independently observed source, sink, cursor and item-entity counts; it does not derive expected totals from the journal being tested. World tests distinguish intent cancellation from actual mutation, including same-tick reversal. Grid properties use injected RNG and persisted permutations; statistical tests have declared sample size/tolerance and reproducible seed, never demand exact short-run uniformity. A passing helper test cannot replace a signed-chat, mutation, proxy switch or rendering gate.

Run applicable formatting, static analysis, units/properties, changed resource/data validation, server-only GameTests, build, dedicated smoke, residual laptop smoke, network/reconnect and artifact/diff inspection in dependency order. Discover actual Gradle tasks from the pinned checkout and inspect their graphs; proposed test classes and commands must become real checked-in harness entries before their phase exits. Use exact failing stimuli, expected invariants, bounded tick/time waits, negative/recovery cases and rerun scope in phase procedures.

Performance acceptance uses representative recorded fixture rates and pack/hardware identities. Compare enabled and disabled diagnostics on identical workloads; diagnostic instrumentation must add no more than one percent p95 CPU time disabled and five percent enabled within declared scope, with no behavioral changes. Audit capture and RTP each target at most two ms p95 added server-thread work per tick at the declared tested workload; p99 spikes, allocation/GC, fsync, backlog, replay and query latency must be published, not hidden by averages. Meeting a time target by sampling mandatory audit events, dropping recent landings or weakening RTP is prohibited. If a representative supported workload exceeds budgets, optimize, apply visible bounded admission where applicable, and rerun; publish the measured capacity envelope. Do not claim unlimited players, events or storage.

Every verification run registers exact owned paths/processes before starting and teardown on every exit path. Use nested disposable directories under the verified project anchor after exact parent Git/build/index/package exclusions, never sibling clones or copied projects. Identify preexisting content in reused directories. Keep only minimal sanitized evidence at `docs/verification/phase-NNN/` and test procedures under `docs/test/`; retain candidate artifacts only through final consumers or as requested deliverables. Stop exact owned processes, verify exit, release locks/tickets/watchers/routes, and remove only verified disposable files without following symlinks. Preserve source, tracked fixtures, shared caches, personal worlds, unrelated worktrees and historical Git. Verify absence on every host. Cleanup failure keeps the gate incomplete even when tests pass. A read-only check that creates nothing records that fact.

Evidence binds source commits, pair/common/protocol digest, loader and dependencies, SQL engine/driver, configuration generation, world/profile identity, host/runtime, fixture seed, test version and decisive outcomes. A material change to any relevant binding invalidates affected evidence and triggers its narrow and dependent reruns. Final merged product commits receive required post-integration verification; pre-merge candidate results alone do not close the endpoint.

## Diagnostics and Debugging

**Requirement IDs:** SEF-REQ-019, SEF-REQ-020, SEF-REQ-024, SEF-REQ-027, SEF-REQ-030, SEF-REQ-035, SEF-REQ-036  
**Task IDs:** P000-TASK-002, P001-TASK-001, P006-TASK-003, P006-TASK-004, P008-TASK-001, P008-TASK-002, P012-TASK-002, P014-TASK-002, P015-TASK-001, P015-TASK-002  
**Controls:** Proposed `sef debug on <category> [target] [seconds]`, `sef debug status`, `sef debug off <capture-id>` and `sef debug off all`, with permission `sef.debug.manage`, console operation, explicit backend/player/operation/grid targets and idempotent disable. Gameplay clients use `/sef`; proxy console uses the same grammar. The Phase 000 external fixture uses its existing bounded launcher/log controls before SEF exists.  
**Signals:** SEF-IF-002 schema and the typed table below bind candidate, side, boot/sequence, desired/actual state, reasons, units and correlation without relying on synchronized clocks. Later subsystems extend the category registry rather than create a second debug framework.  
**Collection procedure:** The numbered local runbook below identifies runtime ownership, enables one scope, reproduces actual entry paths, queries correlation, disables, sanitizes, retains decisive evidence and verifies cleanup.  
**Headless verification:** Discover pinned no-GUI task graphs and invoke the real server command/handler or dedicated GameTest fixture. Console config, queue, journal, grid and authority assertions need no player join. Phase 000 records adapter process identity and handshake logs; real client handshake remains separate.  
**Client verification:** Actual signed chat, native action clicks/hover, tab/entity hiding, private block preview, destination rendering and reconnect require the smallest matching laptop client evidence; server or simulated-player assertions do not close these claims.  
**Client audio isolation:** Before launch of each isolated client, set its pinned master output to zero for prelaunch silence; bind exact Hyprland window/PID, correlate and mute only its PipeWire/Pulse application stream, verify before assertions and reapply on recreation. Missing proof stops the client. Teardown removes owned watcher/routes and verifies process/stream exit.  
**Budgets and privacy:** Off by default and reset on restart/reload; default 60 seconds, maximum 300 seconds, 200 events/second, 10,000 events and 8 MiB per capture, 1024 queued diagnostic events, two simultaneous captures per process. Stop at output/time limits and report drop/truncation counts. Redact secrets, content and private addresses; no diagnostic SQL/I/O on tick/proxy threads. Unsampled mandatory audit is independent of these limits.  
**Regression and support:** Deliver `docs/troubleshooting/diagnostics.md`, linked from README and docs index, with artifact-tested collection and sanitized support packet. Test denial, missing/removed targets, timeout, reload/restart, bounded output, redaction, off-mode overhead and unchanged gameplay before dependent tests.

| Signal | Source and unit | Expected observation |
|---|---|---|
| `command.decision`, `permissionRevision:u64`, `desired.commandId:string`, `actual.state:enum`, `reason:enum` | Backend/proxy owner dispatcher, one per scoped decision | A denial has zero gameplay mutation; allowed result follows its actual terminal operation |
| `bridge.decision`, `channelEpoch:UUID`, `sequence:u64`, `payloadBytes:u32`, `queueDepth:u32` | Transport worker, messages and bytes | Replay/expired/oversize rejection precedes dispatch; queue never exceeds bound |
| `authority.commit`, `expectedRevision:u64`, `actualRevision:u64`, `durationMs:f64` | SQLite writer and replica receiver | One revision per successful operation; gap causes atomic resync, no false acknowledgment |
| `transfer.transition`, `operationId:UUID`, `sessionEpoch:u64`, `desired.backend:string`, `actual.state:enum` | Proxy coordinator and backend owner thread | COMMITTED follows matching destination join and durable arrival; timeout after authorization becomes UNCERTAIN |
| `visibility.decision`, `observerRole:enum`, `surface:enum`, `actual.visible:bool` | Backend/proxy distribution and resolver | Unauthorized observer receives no hidden target on supported surfaces; diagnostic output redacts target location |
| `audit.watermark`, `captured:u64`, `localDurable:u64`, `centralDurable:u64`, `queryVisible:u64`, `oldestPendingMs:u64` | Journal/SQL workers | Ordered durability stages and explicit gaps, never treat enqueue as fsync |
| `audit.loss`, `spoolBytes:u64`, `freeBytes:u64`, `gapFirst:u64?`, `gapLast:u64?`, `reason:enum` | Origin journal manager | Oldest closed owned segment only; unknown extent is null with explicit reason, not zero |
| `restore.step`, `jobId:UUID`, `stepId:UUID`, `expectedDigest:sha256`, `actualDigest:sha256`, `state:enum` | Backend executor and durable job worker | Conflict stops safely; unknown crash state quarantines; counters reflect actual applied groups |
| `rtp.allocation`, `gridGeneration:u64`, `parentCycle:u64`, `childCycle:u64`, `state:enum`, `fencingToken:u64` | Backend serialized authority | One live consuming parent reservation, no reset on child/parent transition, commit only after arrival |
| `rtp.safety`, `minimumDistanceBlocks:f64`, `nearestDistanceSquared:f64?`, `checks:u32`, `tickets:u32`, `workMs:f64` | Backend owner thread | Exact minimum inequality and hazard/claim checks pass at landing; hidden identities omitted |
| `rtp.route`, `sampleAgeMs:u64`, `meanMspt:f64`, `p95Mspt:f64`, `incoming:u32`, `score:f64`, `reason:enum` | Proxy router and authenticated telemetry | Old/forged samples exclude destinations; score factors and capacity lease explain choice |
| `lifecycle.decision`, `visitId:UUID`, `eventId:UUID`, `scope:enum`, `family:enum`, `variantId:string`, `templateGeneration:u64`, `claimState:enum`, `reason:enum`, `recipientCount:u32` | Authority writer and platform dispatch, one scoped decision per event or bounded recipient batch | Failed admission produces no visit, transfer preserves visit, stale callbacks reject, visibility suppresses unauthorized recipients, UNCERTAIN claims never assert client display |
| `capture.status`, `events:u32`, `bytes:u64`, `dropped:u64`, `remainingSeconds:u16`, `output:string` | Diagnostic worker | Timeout/disable stops new records; all limits and actual owned output are visible |

1. Resolve requirement/task, candidate hashes, versions, host and exact runtime. Read the task graph, register owned resources and cleanup, configure and read back `eula=true` for server fixtures, then confirm readiness. Phase 000 uses a 60 second bounded log window around its external adapter scenario and records exact discovered log paths; it does not pretend `sef debug` exists yet.
2. On delivered SEF, use console `sef debug on transfer <operation-id> 60`, or the relevant `command`, `bridge`, `authority`, `visibility`, `lifecycle`, `audit`, `restore` or `rtp` category. For lifecycle reproduction use `sef debug on lifecycle EnVyOnMyMind 60` after resolving that online subject to its current UUID/session; a missing or unauthorized subject refuses capture. Run `sef debug status`; verify side, target, limits and the canonical path under `<runtime>/logs/sef/diagnostics/<capture-id>.jsonl`. Missing targets or unavailable output return an explicit error; do not fall back to unbounded general logs.
3. Reproduce the actual command, packet, world mutation or recovery cut using the phase fixture. For example, interrupt the destination after arrival authorization and before receipt persistence, then restart it with the same world/generation and operation ID. Expected transfer/RTP state is UNCERTAIN until reconciliation. Add only the named residual laptop action after renderer/window/mute and joined-world proof.
4. Filter the discovered JSONL with `jq 'select(.correlationId == "<operation-id>")' <capture-file>` and inspect ordered states, revisions, reason codes, counters and units against the phase oracle. Compare independently observed world/item state rather than trusting the same implementation's counters. A representative event is an illustrative schema, not executed proof: `{"event":"transfer.transition","side":"BACKEND","desired":{"state":"COMMITTED"},"actual":{"state":"UNCERTAIN"},"reason":"ARRIVAL_RECEIPT_MISSING"}`. The complete emitted record also requires the SEF-IF-002 identity fields.
5. Run `sef debug off <capture-id>` and `sef debug status`, trigger one harmless matching observation and verify no further captured event. Record expected/actual, decisive sanitized excerpt, fixture and remaining unverified claims. Redact secrets, full private text, addresses and unrelated identities before retaining a support packet; do not upload automatically.
6. Keep only required evidence under the phase verification directory and the support packet described in `docs/troubleshooting/diagnostics.md`. Stop exact owned clients, watchers, servers/proxy/database fixtures, verify processes/streams are gone, remove their verified disposable outputs after the final consumer and confirm cleanup on both hosts. Preserve reusable diagnostics and documentation. Record exact leftovers separately if cleanup fails.

## 15. Compatibility, Migration, Rollout, and Recovery

### Platform and compatibility

Version product artifacts independently but bind every pair by common contract digest and supported protocol major/minor. Configuration, backend data, SQLite authority, journal, SQL schema, public API, world generation, grid generation, adapter and job payload all carry their own version. Reject future/unknown schemas readably; do not silently coerce. Additive optional protocol fields require negotiated minor support; unknown mandatory types reject. Removal of registry entries invalidates only operations requiring their reconstruction, while sanitized historical metadata remains queryable.

Retained source data migrates through typed explicit adapters into a new owned destination after preflight counts and backup. Preserve original files, economy files and unsupported data unchanged. No Minecraft world downgrade occurs. Local homes import with an explicit backend and current verified world-generation mapping, preserving IDs/revisions where supported and rejecting ambiguous mappings. Transition from local to network authority requires a write fence, dry-run conflict report, explicit source selection, backup and revisioned import; do not combine active competing writers. Downgrade is supported only by restoring the matching backup and artifact pair when schema compatibility permits, never by deleting unknown fields.

Use SQLite's supported backup mechanism or a verified quiesced checkpoint/copy including all required WAL state; copying only a live main file is invalid. Test restoration of network state and backend authority separately from historical SQL and worlds. Restore world snapshots with matching journal/job/grid generation metadata or quarantine affected operations. World replacement creates a new persistent generation even if directory/dimension names match. Audit payload codecs use Forge 1.20.1 NBT with explicit size/depth/registry checks, not 1.21 components or unsafe object deserialization.

Both SQL engines use tested InnoDB, binary UUID/event identities, explicit UTC and collations, parameterized queries and portable text/blob representation. Migrations are versioned transactions where supported; DDL boundaries and restart checkpoints are tested per engine. MySQL-to-MariaDB and reverse migration use empty isolated targets, stop new source writes through an explicit fence while spooling safely, copy by snapshot/watermark, verify row counts and partition hashes, then atomically select the new connection configuration. Failure retains the original source and exact resume checkpoint. Runtime principals have least privilege; temporary migration privileges are separated and revoked. Backup rehearsals include schema, events, blobs, watermarks, coverage/gaps and restoration jobs. Normal purge records scoped selection and intentional gaps and refuses deletion of unresolved-job dependencies without explicit conflict resolution.

Operator instructions distinguish two clean installations. Standalone mode installs the Forge SEF artifact on one server, leaves network mode disabled, and runs local services without Velocity, Ambassador, PCF or client reset. The Ambassador network profile first validates backups, installed proxy/backend/client adapters, the exact compatible client reset mod, profile relations and compatible pair. Start databases and backend journals, establish authenticated bridge and authoritative snapshots, mark destinations ready only after their gates, then enable network commands/RTP. These are documented tested disposable rehearsals, not authorization to change production. A mismatch leaves network destinations unavailable. Backend/proxy restart, SQL outage, torn journal, disk pressure, authority failure and unresolved arrivals each have exact signals, safe state and recovery actions. None is resolved by silently clearing history, bags, permissions or identity.

## 16. Documentation, Operations, and Release Gates

Every phase updates root `README.md`, `docs/README.md`, `DOCUMENTATION.md` and affected topic documents with behavior that exists at its merged revision. Preserve existing casing and established locations. Proposed new topic paths become real when their subsystem exists: `docs/architecture/network.md`, `docs/features/commands/`, `docs/features/audit/`, `docs/features/rtp/`, `docs/configuration/`, `docs/security/network.md`, `docs/migrations/`, `docs/operations/backup-recovery.md`, `docs/troubleshooting/diagnostics.md`, `docs/test/` and `docs/verification/`. Do not create empty category placeholders.

Documentation includes the complete retained/removed action inventory; separate standalone and Ambassador network installation paths, with no external proxy adapters required for one-server use; editable main-config lifecycle families, audiences, variants, safe placeholder catalog, first/returning visit and prior-time semantics, crash-delivery limits and legacy-template/reminder migration; command syntax/aliases/ownership/permissions and source/target semantics; localization and console/accessibility; exact JVM/loader/proxy/backend adapter and external client reset pins with separate installation and missing-capability refusal; common artifact provenance; private forwarding/bridge registration and rotation; compatible pack matrix; SQLite and both SQL dialects; full-content access controls/redaction limits; manual-default retention, explicitly activated scheduling and emergency loss; supported capture and reversible classes; inspector/query/export examples; preview/apply/undo conflicts; recorded-item give consequences; RTP partition/bag/consumption/distance/routing/configuration and tuning; migration/backup/restore/purge/corruption/uncertainty runbooks; extension API examples and supported mod hooks; test host/audio/cleanup procedures; measured capacity and known explicit limitations.

Before each phase implementation, reconcile its existing GitHub milestone/issues/Project fields and required checks using the authenticated EnVisione account. Source commits and annotated tags use EnVy `<contact.enviouse@gmail.com>` as sole author/committer, registered SSH signatures and no coauthors. Git/GitHub prose is lowercase with simple periods and commas; documentation retains normal capitalization and grammar. Preserve literal identifiers/paths when syntax requires them. Use existing central reusable workflow callers, economical standard runners, wrapper validation, formatting/lint/tests, dependency submission/review, CodeQL/credential scans where supported, dependency update coverage and documentation checks. Never add paid services or bypass a capability restriction.

Commit and push completed approved changes to the current applicable phase branch. Create its PR into the product base only under the phase-integration authorization. Require deterministic checks and resolved conversations; one private independent review is additive when available. No public review trigger or public mechanism attribution is permitted. Merge through GitHub using a merge commit, never local fast-forward/direct product push. If pending checks and auto merge are available, enable merge-commit auto merge and wait for actual merge. Fetch, verify the resulting applicable product branch contains that merge, run required branch verification, then create and push a signed annotated phase tag. Paired phases require both branches' gate receipts. Historical branches and tags remain.

Tracked docs are authoritative. Publish corresponding wiki updates only after the approved merge and verify them; finalize issues/Project/milestones after acceptance and integration. Record unavailable optional platform capabilities accurately without pretending they were configured. Final artifact inspection includes no client/economy payload, credentials, incidental caches, absolute development paths or unrelated changes. Retain SHA-256/SHA-512, exact source commits, common/protocol manifest, dependency/license inventory, SPDX SBOM and supported attestations for both artifacts. No CurseForge/Modrinth upload, public GitHub release or production deployment is part of completion.

## 17. Risks and Failure Boundaries

All risk mechanisms below are plausible failure hypotheses unless the evidence column identifies an observed source limitation. Likelihood is qualitative exposure, not an invented probability. High impact requires falsifiable proof even when likelihood is unknown.

| Risk ID and causal scenario | Affected requirements or interfaces | Likelihood and impact rationale | Prevention | Detection signals | Recovery | Owning phase and tasks | Required proof |
|---|---|---|---|---|---|---|---|
| SEF-RISK-001, selected proxy/adapters fail Forge handshake, signed chat or reset switching | SEF-REQ-010, SEF-REQ-018, SEF-IF-005 | Compatibility and mod reset interactions are unverified; a reconnect disguised as transfer has high impact | Independent early pinned fixture, advertised reset capability and no dependent assumption | Login/forwarded UUID/chat/command, proxy result, source exit and destination arrival with continuous client session | Preserve exact failing matrix and block dependent gates; Phase 005 refuses missing capability before movement | SEF-PHASE-000, P000-TASK-002; SEF-PHASE-005, P005-TASK-002 | Real login, chat and two-way in-session transfer; forged identity rejected; missing reset capability refused before movement |
| SEF-RISK-002, excluded economy/interface survives kernel coupling | SEF-REQ-003, SEF-REQ-004 | Observed coupling FIND-103, high scope and startup impact | Inventory transitive construction/config/dependency closure | Command/config/JAR inventory and dedicated linkage | Remove exact residual path, rerun retained behavior | SEF-PHASE-001, P001-TASK-002 | No active surface plus preserved old files and no-SEF-client join |
| SEF-RISK-003, secret or forged/replayed administration crosses bridge | SEF-IF-005, SEF-IF-006 | Public/player input hostile, high privilege impact | Mutual identity, epochs/sequences, typed allowlist and actor recheck | bridge.decision reasons and zero mutation | Revoke peer, fence epoch, resync known state | SEF-PHASE-004, P004-TASK-002 | Forge/replay/oversize/revoked-key frames never reach handlers |
| SEF-RISK-004, out-of-order or lost authority update resurrects stale state | SEF-REQ-012, SEF-IF-006 | Outage/retry expected, high moderation impact | Commit-first CAS, contiguous replicas and atomic snapshot | authority.commit revisions and resync reason | Fence new mutations and install validated snapshot | SEF-PHASE-005, P005-TASK-001 | Crash/reorder/duplicate and corrupt snapshot maintain latest committed truth |
| SEF-RISK-005, signed-chat mute bypass or disconnect | SEF-REQ-014 | Proxy cancellation limitation observed, high functional impact | Backend enforced supported routes, absolute expiry | command/chat denial and actual client receipt | Correct backend route, retain canonical mute state | SEF-PHASE-006, P006-TASK-001 | Signed muted/unmuted public/private/alias paths across restart |
| SEF-RISK-006, vanish leaks in one observer-dependent surface | SEF-REQ-015 | Many independent surfaces, high privacy impact | One observer predicate at all distribution/resolution boundaries | visibility.decision by surface and observer | Resync tracking/tab and invalidate suggestions | SEF-PHASE-006, P006-TASK-002 | Unauthorized real observer sees no target; authorized observer does; explicit mod gaps |
| SEF-RISK-007, stale transfer moves wrong session or false success | SEF-IF-007, SEF-IF-008 | Reconnect and split outcomes expected, high world impact | Stable home/world IDs, session fences and arrival receipts | transfer.transition and destination receipt | Reconcile or quarantine, never timeout-as-nonarrival | SEF-PHASE-007, P007-TASK-001, P007-TASK-002 | Every crash cut, duplicate home names, target switch and world replacement |
| SEF-RISK-008, audit queue or disk silently loses history | SEF-REQ-024, SEF-REQ-026 | Dropping source sinks observed, finite storage certain | Dedicated durable spool, reserved gap ledger and exact rotation boundary | audit.watermark and audit.loss | Replay valid prefixes, publish gap/unknown extent and recover health | SEF-PHASE-008, P008-TASK-001, P008-TASK-002 | Slow/full disk, corrupt tail, SQL outage and loss-ledger failure produce honest watermarks |
| SEF-RISK-009, intent or simulation is misreported as applied mutation | SEF-REQ-023, SEF-REQ-027 | Forge callback limitation observed, high investigation impact | Verified mutation-boundary hooks and independent oracles | outcome, before/after, adapter and confidence fields | Correct adapter, retain explicit affected coverage interval | SEF-PHASE-009, P009-TASK-001 | Late cancellation, simulated transfer, same-tick reversal and changed explosion list |
| SEF-RISK-010, inherited cause context falsely blames a player | SEF-REQ-023, SEF-IF-009 | Async/fake-player paths vary, high attribution impact | Scoped cleared context and explicit propagation | actor/initiator/confidence/parent links | Mark unknown, correct propagation and regression | SEF-PHASE-009, P009-TASK-002 | Concurrent machines/fake players and delayed work never inherit unrelated actor |
| SEF-RISK-011, sensitive content leaks through hover/export/alerts | SEF-REQ-025, SEF-REQ-026, SEF-REQ-031 | Multiple sinks and full content, high privacy impact | Pre-sink schema redaction, distinct rights and per-page checks | access outcomes and secret sentinels | Revoke faulty path, record exposure scope without repeating secret | SEF-PHASE-008, P008-TASK-002; SEF-PHASE-010, P010-TASK-001 | Revocation between pages, nested NBT/commands and all sink scans |
| SEF-RISK-012, restore duplicates items or overwrites newer work | SEF-REQ-028, SEF-IF-011 | Non-atomic world/storage and onward use, high irreversible impact | Fixed selection, fingerprints, causal groups and both-end conservation | restore.step and independent counts | Stop partial job, quarantine uncertain steps, exact job compensation only | SEF-PHASE-011, P011-TASK-001, P011-TASK-002 | Hopper chains, onward crafting, full inventory, entity drops and crash after mutation |
| SEF-RISK-013, rich click acts on new target or injected command | SEF-REQ-030, SEF-IF-004 | Untrusted text and stale UI expected, high privilege impact | Literal components and opaque recipient/session/revision tokens | action rejection reason and operation ID | Expire/reissue safe action after fresh authorization | SEF-PHASE-002, P002-TASK-002 | Markup/separator/Unicode input, theft, repeat click and revoked rights |
| SEF-RISK-014, completion order breaks RTP cycle-boundary fairness | SEF-REQ-032, SEF-IF-012 | Concurrent arrivals expected, high core invariant impact | Actual-last barrier and boundary-first commit gate | rtp.allocation cycles/reservations | Quarantine uncertain entries, continue only after reconciliation | SEF-PHASE-012, P012-TASK-001, P012-TASK-002 | All completion permutations and restart cuts preserve parent/child uniqueness |
| SEF-RISK-015, partition arithmetic or reload reinterprets old cells | SEF-REQ-032, SEF-REQ-035 | Negative/uneven bounds common, high placement impact | Checked intervals and immutable grid generations | generation and exact coordinate ownership | Reject config, retain old state until safe explicit migration | SEF-PHASE-012, P012-TASK-001; SEF-PHASE-014, P014-TASK-002 | Extreme overflow, nonempty partition properties and old-token refusal |
| SEF-RISK-016, distance/hazard changes after preparation | SEF-REQ-033, SEF-IF-013 | Live world changes normal, high player safety impact | Final main-thread checks, all online players and persistent recent window | rtp.safety distance/reason/tickets | Release only proven failure; re-reserve safely or unavailable | SEF-PHASE-013, P013-TASK-001, P013-TASK-002 | Adjacent/vertical/hidden-player races, claim changes, hazards and ticket cleanup |
| SEF-RISK-017, stale telemetry sends a burst to one weak backend | SEF-REQ-034, SEF-IF-014 | Concurrent routing/load variation normal, high availability impact | Fresh authenticated samples, normalized factors and serialized leases | rtp.route sample age/score/incoming | Exclude stale destinations, reconcile leases, no unsafe failover | SEF-PHASE-014, P014-TASK-001 | Unequal capacities, forged/old epochs, burst queue and ambiguous arrival |
| SEF-RISK-018, SQL dialect migration or backup omits required state | SEF-REQ-024, SEF-REQ-029 | Two engines and nontransactional DDL differ, high recovery impact | Exact pins, portable schema, fenced empty-target migration and hashes | migration checkpoint/count/hash and watermark | Retain source, resume or restore matched snapshot | SEF-PHASE-010, P010-TASK-002 | Both directions, interrupted DDL/copy/cutover and complete restored jobs/gaps |
| SEF-RISK-019, diagnostics or high-volume audit stalls gameplay | SEF-REQ-019, SEF-REQ-027, SEF-REQ-035 | Movement and machines generate load, high availability impact | Bounded workers/snapshots and measured tick budgets | workMs, p95/p99, backlog/bytes/drop counters | Tune within invariants, visible admission/degraded capture, no silent sampling | SEF-PHASE-015, P015-TASK-002 | Representative actual workloads with SQL/disk pressure and simultaneous queries |
| SEF-RISK-021, duplicate or stale lifecycle callback exposes hidden presence or invents first visits | SEF-REQ-036, SEF-IF-015 | Existing local callbacks do not prove network semantics; multi-source retries and crashes give high privacy and correctness impact | One authority, UUID visit epochs, post-ready trigger, durable claims, literal per-recipient placeholders and last-good reload | lifecycle.decision scope/visit/family/generation/claim/reason and independent recipient counts | Suppress stale/uncertain delivery, preserve known visits and previous completed timestamps, correct config through atomic validation | SEF-PHASE-006, P006-TASK-003, P006-TASK-004 | Failed login, genuine reconnect versus duplicate callback, backend switch, vanish counts, migrated UUIDs, invalid reload and crash before/after dispatch on real local/network paths |
| SEF-RISK-022, network adapter becomes an accidental standalone dependency | SEF-REQ-001, SEF-REQ-020, SEF-REQ-021 | Shared Forge bootstrap and loader metadata could require PCF or client reset even when no proxy is configured, blocking the common one-server installation | No mandatory adapter metadata or eager adapter class linkage; explicit local/network configuration authority | Loader dependency scan, dedicated startup result and local command/health signals with adapters absent | Remove the hard dependency or eager linkage, preserve configured network fencing, rerun standalone and network regressions | SEF-PHASE-001, P001-TASK-004; SEF-PHASE-004, P004-TASK-003; SEF-PHASE-015, P015-TASK-001 | Final Forge artifact starts and serves local commands, homes, audit and RTP with no proxy, PCF or reset mod; Ambassador profile still passes its real transfer gate |
| SEF-RISK-020, candidate proof is invalidated by merge, pack or host mismatch | SEF-REQ-020, SEF-REQ-022 | Two branches/hosts increase drift exposure, high false-completion impact | Pair manifest, phase gate receipts and post-merge verification | digest/profile/renderer/stream identity | Rerun affected exact gates; keep missing client/cleanup evidence open | SEF-PHASE-015, P015-TASK-001; SEF-PHASE-016, P016-TASK-002 | Wrong pair/profile rejection, actual branch artifact rebuild and per-host cleanup |

There is no universal capture guarantee for invisible mod internals, no universal reversibility guarantee, and no promise of arbitrary pack compatibility or unlimited performance. These limits require explicit matrix entries and observable refusal, not quiet scope deletion. All required capabilities for the selected supported pack and feasible functional parity remain mandatory. Newly discovered material scope conflicts return to owner-authorized plan maintenance; routine evidence and fixes do not rewrite the goal or regenerate plans.

## 18. Definition of Done

Complete the documented server-only Forge 1.20.1 backport and the small SEF Velocity companion, with every retained behavior and network workflow verified on the supported one-proxy three-backend topology, economy and interface surfaces absent, secure durable synchronization and cross-server homes proven, comprehensive activity-history capture, investigation, rollback/restore/undo and declared functional parity proven on MySQL and MariaDB, all-command rich chat verified against supplied references, configurable join/leave and first/returning welcome messages with safe placeholders and network lifecycle semantics proven, and hierarchical grid RTP with strict separation and optional load-aware proxy routing proven, paired artifacts and operator documentation complete, sequential phase pull requests integrated into forge-1.20.1 and velocity-latest as applicable, resulting product branch verification and signed phase tags complete. Public release publication and production rollout are not included.

- All 36 mandatory requirements and their acceptance IDs pass, including every retained command, every applicable parity row, all-command presentation and both RTP modes. Future and excluded scope stays excluded.
- All four main-config lifecycle message families, variants, safe placeholders and previews work in local and network modes with durable UUID classification, correct previous completed-visit values, one authority, visibility-filtered recipients/counts and explicit crash-display ambiguity. Failed admission and backend switches produce no false welcome; legitimate reconnects and migrations preserve the defined semantics.
- Every phase exit, checked PR integration, resulting applicable product-branch verification and required signed phase tag completes sequentially. Both required product integrations close before advancing the global sequence.
- The actual one-proxy three-backend topology passes authenticated forwarding, signed chat moderation, observer-aware vanish, qualified home and consent travel, durable restart/recovery, incompatible-profile refusal and zero-player bridge tests.
- The final Forge artifact also passes a standalone one-server clean installation with no Velocity, Ambassador, PCF or client reset, including retained local commands, storage, homes, lifecycle messages, audit and local RTP. Its loader metadata and JAR contain no mandatory external proxy-adapter dependency.
- Comprehensive audit capture, query, privacy, visible gaps, controlled purge, dual-engine backup/migration, preview/rollback/restore/undo, item conservation and recorded-item give boundaries pass real-path evidence. Unknown or unsupported state is reported honestly and cannot substitute for a feasible mandatory function.
- Parent and independent child RTP cycles, cross-cycle actual-arrival guards, exact partitions, durable reservations/quarantine, strict online/reserved/recent separation, final safety, bounded preparation and fresh capacity routing pass property and world tests without weakened invariants.
- Diagnostics, support commands, budgets, permissions, redaction and artifact-tested runbooks are complete. Required real client claims have actual silent laptop evidence; no server-only surrogate closes input, rendering or synchronization.
- Every launched client remained inaudible with verified owned-stream mute through exit; every test/audit cleanup is verified on all used hosts. Exact unresolved leftovers or unavailable prerequisites keep completion open.
- Final artifacts match approved product commits and shared protocol provenance, include hashes/licenses/SBOM and applicable attestations, and contain neither excluded features nor credentials/incidental outputs. README, technical topics, documentation index and post-merge wiki agree with implemented behavior.
- No known mandatory in-scope defect, failed required check, unresolved required evidence, unmerged required PR or false completeness claim remains. Production rollout and public publication are not performed or implied.

## 19. Goal Creator Handoff

```text
Mandatory boundary: all SEF-REQ-001 through SEF-REQ-036 and their evidence, the complete registered phase sequence, and the owner-selected paired product endpoint.
Optional/future disposition: excluded
Optional/future detail: FUT-001 through FUT-003 remain excluded; NG-001 through NG-005 remain binding.
Locked owner decisions: DEC-001 through DEC-018, plus standing host, silent-client, EULA, cleanup and sequential integration policies.
Active phase: SEF-PHASE-000
Next executable action: P000-TASK-001. Revalidate target/reference identity and prepare the complete retained/removed action inventory, then P000-TASK-002 exact external feasibility before dependent implementation.
Known failing checks: no product runtime tests have been executed in this plan authoring pass. Existing Dependabot update failures are observed repository preflight evidence, not product build results.
Known external blockers: none
Completion endpoint: Complete the documented server-only Forge 1.20.1 backport and the small SEF Velocity companion, with every retained behavior and network workflow verified on the supported one-proxy three-backend topology, economy and interface surfaces absent, secure durable synchronization and cross-server homes proven, comprehensive activity-history capture, investigation, rollback/restore/undo and declared functional parity proven on MySQL and MariaDB, all-command rich chat verified against supplied references, configurable join/leave and first/returning welcome messages with safe placeholders and network lifecycle semantics proven, and hierarchical grid RTP with strict separation and optional load-aware proxy routing proven, paired artifacts and operator documentation complete, sequential phase pull requests integrated into forge-1.20.1 and velocity-latest as applicable, resulting product branch verification and signed phase tags complete. Public release publication and production rollout are not included.
Required evidence gates: per-requirement ACs, phase exits, real path and residual silent-client proof, dual SQL engines, failure/recovery/conservation matrices, measured budgets, private additive review when available, required checks, resulting product commits, signatures and per-host cleanup.
```

The Markdown master and registered phase plans are authoritative; the manifest and handoff are deterministic projections. Execution reads the complete live plan set and a separately created immutable saved goal before acting. No goal or phase cursor is authored by this plan. Once separately created, `docs/plan/goal.md` remains immutable, and only `docs/plan/active_phase.md` advances through the deterministic forward-only helper after all current-phase gates. Progress, evidence, revised plan provenance or a phase transition never authorizes goal refresh, another creator pass or premature next-phase work. Final phase closure requires the complete Definition of Done above.
