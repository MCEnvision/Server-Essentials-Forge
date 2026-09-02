# SEF 2.0.0 Release Candidate Notes

Status: release preparation only. These notes are not a public release declaration.

Target: Minecraft `1.21.1`, NeoForge `21.1.235`, Java `21`.

## Highlights

SEF 2.0 extends the server essentials foundation with server authoritative enhanced GUIs, Fancy Tags, disguise projections, typed server controls, modular configuration, recovery workflows, permission derived cooldowns, and expanded teleport, moderation, economy, inventory, and automation coverage.

## Fixed in this release line

The current verified change set includes fixes for delegated sudo parsing and timing, configuration migration recovery, LuckPerms precedence and failure handling, GUI blur and tooltip duplication, disguise identity and animation paths, offline action authorization, full server admission exemptions, server control runtime honesty, crash recovery claims, legacy enforcement preservation, signed chat provenance, jail and economy sign transitions, Fancy Tags ownership and retention, command catalog parity, `/feed` zero saturation behavior, teleport safety and quotas, and player warp management permission boundaries.

## Compatibility

The artifact is a universal NeoForge JAR. The server remains authoritative. Vanilla and clients without SEF retain command access because the enhanced protocol is optional. LuckPerms, FTB Essentials, and Curios remain optional integrations.

## Migration

Back up the world, `config/sef`, and world SEF storage. Review `/sef config migrate dryrun` before applying any legacy configuration migration. Follow [the migration guide](MIGRATION_GUIDE.md) for confirmation, backup, rollback, and recovery procedures.

## Verification status

The automated unit, GameTest, build, dedicated server startup, headless client startup, generated reference, security, and artifact checks pass on the current source revision. The artifact is `build/libs/sef-2.0.0.jar`, 3,368,441 bytes, with SHA-256 `7acac17497166c5870180521d95fe47cbd1c41a45e33c2dd88b175314d7e2fd4` and SHA-512 `ffd68e24345008c888d80d9b8fcef96eae5d27972b3654b3d1cd1027ee2572536fb779fcd77cf09ecc3e4e57fae3959d0204ef1a7e76dcfc24159e0c0be80e41`. The Phase 14 ledger still has interactive multiplayer, optional integration, GUI visual, upgrade, and rollback rows open. Public publication must wait for those gates and a verified broker preview for both platforms.
