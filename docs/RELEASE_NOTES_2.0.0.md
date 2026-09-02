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

The automated unit, GameTest, build, dedicated server startup, headless client startup, generated reference, security, and artifact checks pass on the current source revision. The artifact is `build/libs/sef-2.0.0.jar`, 3,370,092 bytes, with SHA-256 `4971ac1036ce1b89495cc851b0d7c8720eb2dc04aa20b6b42026fcb3b94195e8` and SHA-512 `be26595f0e2b1c6c1e3fb2ae52d33706012d307d88876383a8e1ed59ba24649ea97ea107cf3787622da7241075d843693e257896b00706f77122c0e3a4747105`. Dependency closure remains blocked pending an owner approved runtime decision. The Phase 14 ledger still has interactive multiplayer, optional integration, GUI visual, upgrade, and rollback rows open. Public publication must wait for those gates and a verified broker preview for both platforms.
