# SEF 2 Unreleased Engineering Preview

Evidence range: commit `81a2e5a` through the current `envy/phase-001-security` working tree. Version metadata is `2.0.0`. These notes are not a public release declaration.

## Added

1. Fancy Tags registry, revisioned artwork storage, bounded import and transfer paths, assignments, local project tools, negotiated client cache, rendering bridges, and command fallback.
2. Server authoritative disguise definitions, projections, vanilla proxy support, curated traits and abilities, sound policy, persistence, commands, and negotiated client state.
3. Typed server control schemas and bounded workflows for operations, community, onboarding, recovery, governance, access, world policy, diagnostics, privacy, market, knowledge, and display ownership.
4. Recovery services for graves and inventory snapshots, plus approval, access lease, administrative lock, community state, and server control repositories.
5. Permission derived cooldown resolution with deterministic precedence, finite fallback, persistence, wildcard diagnostics, and route sharing across canonical actions.
6. A 62 module typed configuration registry under `config/sef/modules`, transactional reload, retained history, typed command editing, GUI policy overrides, watcher support, and generated defaults.
7. Registry generated configuration, command, permission, and default directory references with exact drift tests.
8. Staged legacy `common.toml` migration with source hash confirmation, complete candidate validation, exact recovery backups, failure restoration, retained legacy input, and an idempotent marker.

## Changed

1. Administrative enchanting supports the reviewed extended level range and the dedicated advanced workflow while keeping ordinary enchanting separate.
2. Delegated sudo uses immutable one execution grants, explicit `respect` and `delegate` grammar, exact profile limits, confirmation, target notification, provider diagnostics, and complete audit lifecycle events.
3. GUI and HUD protocol families include expanded control editor, Fancy Tags, disguise, configuration, and server control projections with bounded codecs and revision checks.
4. Module file rewrites preserve existing POSIX permissions and retain unknown bounded fields.
5. Generated reference output is deterministic across JVM processes and machines.

## Fixed

1. Fixed delegated mode being consumed as part of a greedy sudo command argument.
2. Fixed future dated delegated grants being consumable before their valid interval.
3. Fixed detectable hard linked module and migration files being accepted.
4. Fixed configuration documentation upgrades lacking a fixed recovery backup and exact materialization path.
5. Fixed generated reference drift caused by unordered dependency and conflict sets.
6. Fixed LuckPerms broad wildcard grants, exact denial precedence, and transient NeoForge permission bridge failures.
7. Fixed SEF screen blur and duplicate item-picker tooltips.
8. Fixed disguise root suggestions, namespaced entity ids, misleading ability errors, proxy movement, and animation synchronization.
9. Fixed offline queued actions running before the target was online or through a console authority escalation.
10. Fixed full-server admission exemptions being rejected by Minecraft before the SEF queue or exemption policy could run.
11. Fixed server-control schemas being reported as executable without a real runtime handler and blocked manual activation from impersonating execution.
12. Fixed server-control and queued-action crash windows with durable operation claims, receipts, ambiguous-outcome blocking, and restart reconciliation.
13. Fixed legacy enforcement stores clearing live bans, mutes, filters, warnings, MOTD, bulletins, alternate-account state, announcements, or profiles after damaged input.
14. Fixed public chat losing signed-player provenance and fixed `/ans` revealing vanished identity, content, or presence.
15. Fixed managed JSON depth exhaustion, symbolic-link traversal, file-open races, export boundary checks, and incomplete atomic rename durability.
16. Fixed jail transition ordering, unsafe destination recovery, economy sign cancellation, mute restart extension, and invalid mute record handling.
17. Fixed quadratic disguise synchronization, synchronous entity diagnostics, dead active-entity mixin state, and unbounded diagnostic refresh.
18. Fixed private chat retention, Fancy Tag upload ownership ordering, markdown escaping, and hidden exception diagnostics.
19. Fixed command catalog, shortcut, GUI workflow, source-policy, and live-dispatcher disagreement across the complete registered command surface.
20. Fixed `/setworth` parsing for namespaced item identifiers and narrowed NeoForge metadata to the verified `21.1.235` version.
21. Fixed `/feed` granting saturation and causing a fast natural-regeneration burst. It now fills hunger with zero saturation, leaves health unchanged, and has live-dispatcher GameTest coverage.
22. Fixed secondary GUI request entries so accept and deny actions are independently permission gated and stale entries revalidate the current command permission.
23. Fixed random teleport safety to enforce a 20,000 block maximum, surface destinations, and unconditional water rejection.
24. Added expanded permission tiers for homes and player warps and preserved the LuckPerms per-dimension metadata alias during migration.
25. Fixed home replacement moving a named home into a full destination dimension by enforcing the destination quota at the repository boundary.
26. Fixed `/pwarp info` and `/pwarp visits` exposing management data when only ordinary player-warp use permission was granted.

## Migration

1. Back up the world, `config/sef`, and world SEF storage.
2. Review `/sef config migrate dryrun`.
3. Resolve every validation or dependency error.
4. Request `/sef config migrate apply <expected_revision>` and complete its exact confirmation.
5. Review `config/sef/modules/migration.toml` and the backups under `config/sef/backups/configuration/modular-migration-1`.
6. Move legacy cooldown intent to `sef.cooldown.<action>.<seconds>` permissions.

See [the migration guide](MIGRATION_GUIDE.md) for recovery and rollback details.

## Compatibility

The target is Minecraft `1.21.1`, NeoForge `21.1.235`, and Java `21`. LuckPerms, FTB Essentials, and Curios remain optional. The recorded LuckPerms NeoForge `5.4.140` player-placement failure occurred on NeoForge `21.1.233`; current integration compatibility on `21.1.235` remains unverified. The same universal JAR may be installed on compatible clients for enhanced presentation, while vanilla and non-SEF clients retain command access.

## Verification state

All 534 unit tests, all 41 required GameTests, registration checks for all 694 catalog actions and 315 shortcuts, 2,213 representative parser variants, 358 safe read only live routes, the Java 21 build, fallback-runtime compilation, generated references, dedicated-server startup, headless client startup, security scans, and current artifact inspection pass. The current artifact is `build/libs/sef-2.0.0.jar`. All twenty confirmed source findings in `audit.md` are repaired. The audit writer now uses a platform native descriptor provider on Linux, macOS, and Windows through the JNA API supplied by the pinned NeoForge runtime. The current pull request matrix passed the same candidate artifact, native provider smoke, and disposable writer probe on all three operating systems. The Minecraft Java client fixture passed on Ubuntu using that artifact. External platform dependency ownership remains open under `EXT-002`. Release acceptance does not pass yet. Sixteen Phase 13 runtime families remain unavailable, and the renewed multiplayer, LuckPerms, GUI visual, InvSee, admission-capacity, and disguise-animation matrices are still open. The authoritative phase record is [the acceptance ledger](SEF2_ACCEPTANCE.md).
