# SEF 2 Unreleased Engineering Preview

Evidence range: commit `81a2e5a` through the current `envy/sef2_complete` working tree. Version metadata remains `1.0-SNAPSHOT`. These notes are not a public release declaration.

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

## Migration

1. Back up the world, `config/sef`, and world SEF storage.
2. Review `/sef config migrate dryrun`.
3. Resolve every validation or dependency error.
4. Request `/sef config migrate apply <expected_revision>` and complete its exact confirmation.
5. Review `config/sef/modules/migration.toml` and the backups under `config/sef/backups/configuration/modular-migration-1`.
6. Move legacy cooldown intent to `sef.cooldown.<action>.<seconds>` permissions.

See [the migration guide](MIGRATION_GUIDE.md) for recovery and rollback details.

## Compatibility

The target remains Minecraft `1.21.1`, NeoForge `21.1.233`, and Java `21`. LuckPerms, FTB Essentials, and Curios remain optional. The same universal JAR may be installed on compatible clients for enhanced presentation, while vanilla and non-SEF clients retain command access.

## Verification state

All 390 unit tests, all 29 required GameTests, Java 21 build, dedicated-server checks, enhanced and fallback client matrix, migration and recovery fixtures, integration isolation, generated performance budgets, live tick profiling, security review, generated-reference drift, and final artifact inspection pass. The authoritative phase record is [the acceptance ledger](SEF2_ACCEPTANCE.md).
